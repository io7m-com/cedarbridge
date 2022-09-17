/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.cedarbridge.bridgedoc.xhtml.internal;

import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorConfiguration;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorException;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorResult;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorType;
import com.io7m.cedarbridge.schema.compiled.CBExternalType;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionApplication;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;
import static javax.xml.transform.OutputKeys.ENCODING;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.METHOD;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.transform.OutputKeys.VERSION;

/**
 * An XHTML generator.
 */

public final class CBXGenerator implements CBSPIDocGeneratorType
{
  private static final String XHTML =
    "http://www.w3.org/1999/xhtml";

  private final CBSPIDocGeneratorConfiguration configuration;
  private Map<String, CBPackageType> packages;
  private Map<String, CBDocumentedPackage> documented;

  /**
   * An XHTML generator.
   *
   * @param inConfiguration The configuration
   */

  public CBXGenerator(
    final CBSPIDocGeneratorConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public CBSPIDocGeneratorResult execute(
    final CBPackageType pack)
    throws CBSPIDocGeneratorException
  {
    try {
      final var documentBuilders =
        DocumentBuilderFactory.newDefaultInstance();
      final var documentBuilder =
        documentBuilders.newDocumentBuilder();

      this.packages =
        this.collectPackages(pack)
          .distinct()
          .collect(Collectors.toMap(CBPackageType::name, Function.identity()));

      this.documented =
        this.packages.values()
          .stream()
          .map(p -> Map.entry(
            p.name(),
            this.createDocumentedPackage(p, documentBuilder)))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      for (final var p : this.packages.values()) {
        this.processPackage(p);
      }

      final var files = new ArrayList<Path>();
      for (final var d : this.documented.values()) {
        files.add(this.serializePackage(d));
      }

      return new CBSPIDocGeneratorResult(List.copyOf(files));
    } catch (final Exception e) {
      throw new CBSPIDocGeneratorException(e);
    }
  }

  private Path serializePackage(
    final CBDocumentedPackage documentedPackage)
    throws Exception
  {
    final var fileOutput =
      this.configuration.outputDirectory()
        .resolve(documentedPackage.name() + ".xhtml");

    final var source = new DOMSource(documentedPackage.document());
    try (var output = Files.newBufferedWriter(fileOutput)) {
      final var result =
        new StreamResult(output);
      final var transformerFactory =
        TransformerFactory.newInstance();

      final var transformer =
        transformerFactory.newTransformer();

      output.append(xmlDirective());
      output.newLine();
      output.append(xmlDoctype());
      output.newLine();

      transformer.setOutputProperty(ENCODING, "UTF-8");
      transformer.setOutputProperty(VERSION, "1.0");
      transformer.setOutputProperty(METHOD, "xml");
      transformer.setOutputProperty(INDENT, "yes");
      transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
      transformer.transform(source, result);
      output.flush();
    }

    return fileOutput;
  }

  private static String xmlDoctype()
  {
    return """
      <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
            """.strip();
  }

  private static String xmlDirective()
  {
    return """  
      <?xml version="1.0" encoding="UTF-8"?>
      """.strip();
  }

  private CBDocumentedPackage createDocumentedPackage(
    final CBPackageType pack,
    final DocumentBuilder documentBuilder)
  {
    final var document =
      documentBuilder.newDocument();

    final var root =
      document.createElementNS(XHTML, "html");
    root.setAttribute("xml:lang", "en");
    document.appendChild(root);

    final var head = document.createElementNS(XHTML, "head");
    root.appendChild(head);

    final var meta = document.createElementNS(XHTML, "meta");
    meta.setAttribute("http-equiv", "content-type");
    meta.setAttribute("content", "application/xhtml+xml; charset=utf-8");
    head.appendChild(meta);

    {
      final var style = document.createElementNS(XHTML, "link");
      style.setAttribute("rel", "stylesheet");
      style.setAttribute("type", "text/css");
      style.setAttribute("href", "reset.css");
      head.appendChild(style);
    }

    {
      final var style = document.createElementNS(XHTML, "link");
      style.setAttribute("rel", "stylesheet");
      style.setAttribute("type", "text/css");
      style.setAttribute("href", "document.css");
      head.appendChild(style);
    }

    {
      final var title = document.createElementNS(XHTML, "title");
      title.setTextContent(pack.name());
      head.appendChild(title);
    }

    final var body = document.createElementNS(XHTML, "body");
    root.appendChild(body);

    final var bodyMain = document.createElementNS(XHTML, "div");
    bodyMain.setAttribute("class", "cbPackage");
    bodyMain.setAttribute("id", "main");
    body.appendChild(bodyMain);

    return new CBDocumentedPackage(
      document,
      bodyMain,
      pack.name(),
      new HashMap<>(),
      new HashMap<>()
    );
  }

  private void processPackage(
    final CBPackageType p)
  {
    final var documentedPackage =
      this.documented.get(p.name());

    final var doc =
      documentedPackage.document;

    {
      final var title = doc.createElementNS(XHTML, "h1");
      final var a = doc.createElementNS(XHTML, "a");
      a.setAttribute("href", "#main");
      a.setTextContent(p.name());
      title.appendChild(a);
      documentedPackage.bodyMain.appendChild(title);
    }

    this.processPackageTypes(p, documentedPackage, doc);
    processPackageProtocols(p, documentedPackage, doc);
  }

  private static void processPackageProtocols(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPackage,
    final Document doc)
  {
    final var pProtocols =
      pack.protocols()
        .values()
        .stream()
        .sorted(Comparator.comparing(CBProtocolDeclarationType::name))
        .toList();

    if (!pProtocols.isEmpty()) {
      final var cc = doc.createElementNS(XHTML, "div");
      cc.setAttribute("class", "cbProtocolList");
      cc.setAttribute("id", "protocols");

      {
        final var title = doc.createElementNS(XHTML, "h2");
        final var a = doc.createElementNS(XHTML, "a");
        a.setAttribute("href", "#protocols");
        a.setTextContent("Protocols");
        title.appendChild(a);
        cc.appendChild(title);
      }

      for (final var t : pProtocols) {
        processProtocol(pack, documentedPackage, t);
      }

      final var protocols =
        documentedPackage.protocols.values()
          .stream()
          .sorted(Comparator.comparing(o -> o.protocol.name()))
          .toList();

      {
        final var ul = doc.createElementNS(XHTML, "ul");
        for (final var p : protocols) {
          final var li =
            doc.createElementNS(XHTML, "li");
          final var a =
            doc.createElementNS(XHTML, "a");

          a.setAttribute("href", anchor(p.protocol()));
          a.setTextContent(p.protocol().name());

          li.appendChild(a);
          ul.appendChild(li);
        }
        cc.appendChild(ul);
      }

      documentedPackage.bodyMain.appendChild(cc);
      for (final var t : protocols) {
        documentedPackage.bodyMain.appendChild(t.element);
      }
    }
  }

  private static void processProtocol(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBProtocolDeclarationType protocol)
  {
    final var doc =
      documentedPack.document;
    final var holder =
      doc.createElementNS(XHTML, "div");
    holder.setAttribute("class", "cbProtocol");

    final var docProto = new CBDocumentedProtocol(protocol, holder);
    holder.setAttribute("id", id(protocol));

    final var header =
      doc.createElementNS(XHTML, "h3");
    final var anchor =
      doc.createElementNS(XHTML, "a");

    anchor.setAttribute("href", anchor(protocol));
    anchor.setTextContent(protocol.name());
    header.appendChild(anchor);
    holder.appendChild(header);

    for (final var docText : protocol.documentation()) {
      final var p =
        documentedPack.document.createElementNS(XHTML, "p");
      p.setTextContent(docText);
      holder.appendChild(p);
    }

    final var versions =
      protocol.versions()
        .values()
        .stream()
        .sorted(Comparator.comparing(CBProtocolVersionDeclarationType::version))
        .toList();

    for (final var version : versions) {
      holder.appendChild(processProtocolVersion(doc, version));
    }

    documentedPack.protocols.put(protocol.name(), docProto);
  }

  private static Element processProtocolVersion(
    final Document doc,
    final CBProtocolVersionDeclarationType version)
  {
    final var cc = doc.createElementNS(XHTML, "div");
    cc.setAttribute("class", "cbProtocolVersion");

    final var title = doc.createElementNS(XHTML, "h4");
    title.setTextContent("Version %s Messages".formatted(version.version()));
    cc.appendChild(title);

    final var table = doc.createElementNS(XHTML, "table");
    table.setAttribute("class", "cbProtocolTable");
    cc.appendChild(table);

    {
      final var thead =
        doc.createElementNS(XHTML, "thead");
      table.appendChild(thead);

      final var tr =
        doc.createElementNS(XHTML, "tr");
      final var tname =
        doc.createElementNS(XHTML, "th");
      final var tdesc =
        doc.createElementNS(XHTML, "th");

      tname.setTextContent("Name");
      tdesc.setTextContent("Description");

      tr.appendChild(tname);
      tr.appendChild(tdesc);
      thead.appendChild(tr);
    }

    {
      final var tbody =
        doc.createElementNS(XHTML, "tbody");
      table.appendChild(tbody);

      for (final var type : version.types()) {
        final var tr =
          doc.createElementNS(XHTML, "tr");
        tbody.appendChild(tr);

        final var tname =
          doc.createElementNS(XHTML, "td");
        final var tdesc =
          doc.createElementNS(XHTML, "td");

        tr.appendChild(tname);
        tr.appendChild(tdesc);

        final var a =
          doc.createElementNS(XHTML, "a");
        final var typeDecl = type.declaration();
        a.setAttribute("href", anchor(typeDecl));
        a.setTextContent(typeDecl.name());
        tname.appendChild(a);

        final var pDoc = typeDecl.documentation();
        if (!pDoc.isEmpty()) {
          tdesc.appendChild(processDocumentationInline(doc, pDoc));
        } else {
          tdesc.appendChild(doc.createTextNode("No description provided."));
        }
      }
    }

    return cc;
  }

  private void processPackageTypes(
    final CBPackageType p,
    final CBDocumentedPackage documentedPackage,
    final Document doc)
  {
    final var pTypes =
      p.types()
        .values()
        .stream()
        .sorted(Comparator.comparing(CBTypeDeclarationType::name))
        .toList();

    if (!pTypes.isEmpty()) {
      final var cc = doc.createElementNS(XHTML, "div");
      cc.setAttribute("class", "cbTypeList");
      cc.setAttribute("id", "types");

      {
        final var title = doc.createElementNS(XHTML, "h2");
        final var a = doc.createElementNS(XHTML, "a");
        a.setAttribute("href", "#types");
        a.setTextContent("Types");
        title.appendChild(a);
        cc.appendChild(title);
      }

      for (final var t : pTypes) {
        this.processType(p, documentedPackage, t);
      }

      final var types =
        documentedPackage.types.values()
          .stream()
          .sorted(Comparator.comparing(o -> o.type.name()))
          .toList();

      {
        final var ul = doc.createElementNS(XHTML, "ul");
        for (final var t : types) {
          final var li =
            doc.createElementNS(XHTML, "li");
          final var a =
            doc.createElementNS(XHTML, "a");

          a.setAttribute("href", anchor(t.type()));
          a.setTextContent(t.type().name());

          li.appendChild(a);
          ul.appendChild(li);
        }
        cc.appendChild(ul);
      }

      documentedPackage.bodyMain.appendChild(cc);
      for (final var t : types) {
        documentedPackage.bodyMain.appendChild(t.element);
      }
    }
  }

  private void processType(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBTypeDeclarationType t)
  {
    final var holder =
      documentedPack.document.createElementNS(XHTML, "div");
    holder.setAttribute("class", "cbType");

    final var docType = new CBDocumentedType(t, holder);
    holder.setAttribute("id", id(t));

    final var header =
      documentedPack.document.createElementNS(XHTML, "h3");
    final var anchor =
      documentedPack.document.createElementNS(XHTML, "a");

    anchor.setAttribute("href", anchor(t));
    anchor.setTextContent(t.name());

    header.appendChild(anchor);
    holder.appendChild(header);

    documentedPack.types.put(t.name(), docType);

    for (final var doc : t.documentation()) {
      final var p =
        documentedPack.document.createElementNS(XHTML, "p");
      p.setTextContent(doc);
      holder.appendChild(p);
    }

    if (t instanceof CBRecordType rec) {
      holder.appendChild(
        processTypeRecord(pack, documentedPack, rec, docType)
      );
    } else if (t instanceof CBVariantType var) {
      holder.appendChild(
        this.processTypeVariant(pack, documentedPack, var, docType)
      );
    } else if (t instanceof CBExternalType ext) {
      holder.appendChild(
        processTypeExternal(pack, documentedPack, ext, docType)
      );
    } else {
      throw new IllegalStateException();
    }
  }

  private static Node processTypeExternal(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBExternalType ext,
    final CBDocumentedType documented)
  {
    final var d = documentedPack.document;
    final var e = d.createElementNS(XHTML, "div");

    e.appendChild(renderTypeExternal(pack, documentedPack, ext, documented));

    processTypeParameters(documentedPack, ext, documented)
      .ifPresent(e::appendChild);

    return e;
  }

  private static Optional<Element> processTypeParameters(
    final CBDocumentedPackage documentedPack,
    final CBTypeDeclarationType t,
    final CBDocumentedType documentedType)
  {
    final var parameters = t.parameters();
    if (!parameters.isEmpty()) {
      final var doc = documentedPack.document;
      final var table =
        doc.createElementNS(XHTML, "table");

      table.setAttribute("class", "cbParameterTable");

      {
        final var thead =
          doc.createElementNS(XHTML, "thead");
        table.appendChild(thead);

        final var tr =
          doc.createElementNS(XHTML, "tr");
        final var tname =
          doc.createElementNS(XHTML, "th");
        final var tdesc =
          doc.createElementNS(XHTML, "th");

        tname.setTextContent("Name");
        tdesc.setTextContent("Description");

        tr.appendChild(tname);
        tr.appendChild(tdesc);
        thead.appendChild(tr);
      }

      {
        final var tbody =
          doc.createElementNS(XHTML, "tbody");
        table.appendChild(tbody);

        for (final var param : parameters) {
          final var tr =
            doc.createElementNS(XHTML, "tr");
          tbody.appendChild(tr);

          final var tname =
            doc.createElementNS(XHTML, "td");
          final var tdesc =
            doc.createElementNS(XHTML, "td");

          tr.appendChild(tname);
          tr.appendChild(tdesc);

          final var a =
            doc.createElementNS(XHTML, "a");
          a.setAttribute("id", idPlus(t, param.name()));
          a.setAttribute("href", anchorPlus(t, param.name()));
          a.setTextContent(param.name());
          tname.appendChild(a);

          final var pDoc = param.documentation();
          if (!pDoc.isEmpty()) {
            tdesc.appendChild(processDocumentationInline(doc, pDoc));
          } else {
            tdesc.appendChild(doc.createTextNode("No description provided."));
          }
        }
      }

      final var pHolder =
        doc.createElementNS(XHTML, "div");
      final var pTitle =
        doc.createElementNS(XHTML, "h4");

      pTitle.setTextContent("Parameters");
      pHolder.appendChild(pTitle);
      pHolder.appendChild(table);
      return Optional.of(pHolder);
    }
    return Optional.empty();
  }

  private static Element processDocumentationInline(
    final Document document,
    final List<String> documentation)
  {
    final var e = document.createElementNS(XHTML, "span");
    e.setTextContent(String.join(" ", documentation));
    return e;
  }

  private Element processTypeVariant(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBVariantType var,
    final CBDocumentedType documentedType)
  {
    final var d = documentedPack.document;
    final var e = d.createElementNS(XHTML, "div");

    e.appendChild(renderTypeVariant(pack, documentedPack, var, documentedType));

    processTypeParameters(documentedPack, var, documentedType)
      .ifPresent(e::appendChild);

    final var cases = var.cases();
    if (!cases.isEmpty()) {
      final var casesTitle =
        d.createElementNS(XHTML, "h4");
      casesTitle.setTextContent("Cases");
      e.appendChild(casesTitle);

      for (final var caseV : cases) {
        final var cc =
          d.createElementNS(XHTML, "div");
        final var caseTitle =
          d.createElementNS(XHTML, "h5");
        final var nc =
          d.createElementNS(XHTML, "span");
        nc.setTextContent(caseV.name());

        caseTitle.appendChild(d.createTextNode("Case "));
        caseTitle.appendChild(nc);
        cc.appendChild(caseTitle);

        final var fields = caseV.fields();
        if (fields.isEmpty()) {
          final var name = d.createElementNS(XHTML, "span");
          name.setTextContent(caseV.name());
          name.setAttribute("class", "cbCaseName");

          final var p = d.createElementNS(XHTML, "p");
          p.appendChild(d.createTextNode("The case "));
          p.appendChild(name);
          p.appendChild(d.createTextNode(" has no fields."));
          cc.appendChild(p);
        } else {
          cc.appendChild(processFields(pack, documentedPack, var, fields));
        }

        e.appendChild(cc);
      }
    }

    return e;
  }

  private static Element processTypeRecord(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBRecordType rec,
    final CBDocumentedType documentedType)
  {
    final var e =
      documentedPack.document.createElementNS(XHTML, "div");

    e.appendChild(renderTypeRecord(pack, documentedPack, rec, documentedType));

    processTypeParameters(documentedPack, rec, documentedType)
      .ifPresent(e::appendChild);

    final var fieldsTitle =
      documentedPack.document.createElementNS(XHTML, "h4");
    fieldsTitle.setTextContent("Fields");
    e.appendChild(fieldsTitle);

    final var fields = rec.fields();
    e.appendChild(processFields(pack, documentedPack, rec, fields));
    return e;
  }

  private static Element processFields(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBTypeDeclarationType type,
    final List<CBFieldType> fields)
  {
    final var table =
      documentedPack.document.createElementNS(XHTML, "table");

    table.setAttribute("class", "cbFieldsTable");

    {
      final var thead =
        documentedPack.document.createElementNS(XHTML, "thead");
      table.appendChild(thead);

      final var tr =
        documentedPack.document.createElementNS(XHTML, "tr");
      final var tname =
        documentedPack.document.createElementNS(XHTML, "th");
      final var ttype =
        documentedPack.document.createElementNS(XHTML, "th");
      final var tdesc =
        documentedPack.document.createElementNS(XHTML, "th");

      tname.setTextContent("Name");
      ttype.setTextContent("Type");
      tdesc.setTextContent("Description");

      tr.appendChild(tname);
      tr.appendChild(ttype);
      tr.appendChild(tdesc);
      thead.appendChild(tr);
    }

    {
      final var tbody =
        documentedPack.document.createElementNS(XHTML, "tbody");
      table.appendChild(tbody);

      for (final var field : fields) {
        final var tr =
          documentedPack.document.createElementNS(XHTML, "tr");
        final var tname =
          documentedPack.document.createElementNS(XHTML, "td");
        final var ttype =
          documentedPack.document.createElementNS(XHTML, "td");
        final var tdesc =
          documentedPack.document.createElementNS(XHTML, "td");

        final var anchorName =
          documentedPack.document.createElementNS(XHTML, "a");
        anchorName.setTextContent(field.name());

        final var owner = field.fieldOwner();
        if (owner instanceof CBRecordType rec) {
          anchorName.setAttribute("id", idPlus(type, field.name()));
          anchorName.setAttribute("href", anchorPlus(type, field.name()));
        } else if (owner instanceof CBVariantCaseType vcase) {
          anchorName.setAttribute(
            "id",
            idPlus(type, "%s_%s".formatted(vcase.name(), field.name())));
          anchorName.setAttribute(
            "href",
            anchorPlus(type, "%s_%s".formatted(vcase.name(), field.name())));
        }

        tname.appendChild(anchorName);
        ttype.appendChild(
          processTypeExpression(documentedPack.document, pack, field.type())
        );
        tdesc.appendChild(
          processDocumentationInline(
            documentedPack.document,
            field.documentation())
        );

        tr.appendChild(tname);
        tr.appendChild(ttype);
        tr.appendChild(tdesc);
        tbody.appendChild(tr);
      }
    }
    return table;
  }

  private static Element renderTypeRecord(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBRecordType rec,
    final CBDocumentedType documentedType)
  {
    final var d = documentedPack.document;
    final var e = d.createElementNS(XHTML, "div");
    e.setAttribute("class", "cbTypeOverview");

    {
      final var a = d.createElementNS(XHTML, "a");
      a.setAttribute("href", anchor(rec));
      a.setTextContent(rec.name());

      final var kw = d.createElementNS(XHTML, "span");
      kw.setAttribute("class", "cbKeyword");
      kw.setTextContent("record");

      final var e0 = d.createElementNS(XHTML, "div");
      e0.setAttribute("class", "cbTypeOverviewRecordLine");
      e0.appendChild(bracketOpen(d, '('));
      e0.appendChild(kw);
      e0.appendChild(a);
      e.appendChild(e0);
    }

    for (final var parameter : rec.parameters()) {
      final var a = d.createElementNS(XHTML, "a");
      a.setAttribute("href", anchorPlus(rec, parameter.name()));
      a.setTextContent(parameter.name());

      final var kw = d.createElementNS(XHTML, "span");
      kw.setAttribute("class", "cbKeyword");
      kw.setTextContent("parameter");

      final var e0 = d.createElementNS(XHTML, "div");
      e0.setAttribute("class", "cbTypeOverviewParameterLine");
      e0.appendChild(bracketOpen(d, '['));
      e0.appendChild(kw);
      e0.appendChild(a);
      e0.appendChild(bracketClose(d, ']', false));
      e.appendChild(e0);
    }

    for (final var field : rec.fields()) {
      final var a = d.createElementNS(XHTML, "a");
      a.setAttribute("href", anchorPlus(rec, field.name()));
      a.setTextContent(field.name());

      final var kw = d.createElementNS(XHTML, "span");
      kw.setAttribute("class", "cbKeyword");
      kw.setTextContent("field");

      final var e0 = d.createElementNS(XHTML, "div");
      e0.setAttribute("class", "cbTypeOverviewFieldLine");
      e0.appendChild(bracketOpen(d, '['));
      e0.appendChild(kw);
      e0.appendChild(a);
      e0.appendChild(processTypeExpression(d, pack, field.type()));
      e0.appendChild(bracketClose(d, ']', false));
      e.appendChild(e0);
    }

    {
      final var e0 = d.createElementNS(XHTML, "div");
      e0.appendChild(bracketClose(d, ')', true));
      e.appendChild(e0);
    }

    return e;
  }

  private static Element bracketOpen(
    final Document d,
    final char type)
  {
    final var e = d.createElementNS(XHTML, "span");
    e.setAttribute("class", "cbBracketOpen");
    e.setTextContent("" + type);
    return e;
  }

  private static Element bracketClose(
    final Document d,
    final char type,
    final boolean trailing)
  {
    final var e = d.createElementNS(XHTML, "span");
    if (trailing) {
      e.setAttribute("class", "cbBracketClose cbBracketTrailing");
    } else {
      e.setAttribute("class", "cbBracketClose");
    }
    e.setTextContent("" + type);
    return e;
  }

  private static Element renderTypeExternal(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBExternalType ext,
    final CBDocumentedType documented)
  {
    final var d = documentedPack.document;
    final var e = d.createElementNS(XHTML, "div");
    e.setAttribute("class", "cbTypeOverview");

    {
      final var e0 = d.createElementNS(XHTML, "div");
      e0.appendChild(bracketOpen(d, '['));
      e0.appendChild(d.createTextNode("external"));
      e0.appendChild(d.createTextNode(" "));
      e0.appendChild(d.createTextNode(ext.name()));
      e.appendChild(e0);

      for (final var parameter : ext.parameters()) {
        e.appendChild(processParameter(ext, d, parameter));
      }

      e.appendChild(bracketClose(d, ']', true));
    }

    return e;
  }

  private static Element processParameter(
    final CBTypeDeclarationType ext,
    final Document d,
    final CBTypeParameterType parameter)
  {
    final var a = d.createElementNS(XHTML, "a");
    a.setAttribute("href", anchorPlus(ext, parameter.name()));
    a.setTextContent(parameter.name());

    final var kw = d.createElementNS(XHTML, "span");
    kw.setAttribute("class", "cbKeyword");
    kw.setTextContent("parameter");

    final var e1 = d.createElementNS(XHTML, "div");
    e1.setAttribute("class", "cbTypeOverviewParameterLine");
    e1.appendChild(bracketOpen(d, '['));
    e1.appendChild(kw);
    e1.appendChild(a);
    e1.appendChild(bracketClose(d, ']', false));
    return e1;
  }

  private static Element renderTypeVariant(
    final CBPackageType pack,
    final CBDocumentedPackage documentedPack,
    final CBVariantType var,
    final CBDocumentedType documentedType)
  {
    final var d = documentedPack.document;
    final var e = d.createElementNS(XHTML, "div");
    e.setAttribute("class", "cbTypeOverview");

    {
      final var a = d.createElementNS(XHTML, "a");
      a.setAttribute("href", anchor(var));
      a.setTextContent(var.name());

      final var kw = d.createElementNS(XHTML, "span");
      kw.setAttribute("class", "cbKeyword");
      kw.setTextContent("variant");

      final var e0 = d.createElementNS(XHTML, "div");
      e0.setAttribute("class", "cbTypeOverviewVariantLine");
      e0.appendChild(bracketOpen(d, '('));
      e0.appendChild(kw);
      e0.appendChild(a);
      e.appendChild(e0);
    }

    for (final var parameter : var.parameters()) {
      e.appendChild(processParameter(var, d, parameter));
    }

    for (final var caseV : var.cases()) {
      final var e0 = d.createElementNS(XHTML, "div");
      e0.setAttribute("class", "cbTypeOverviewVariantCaseLine");
      e0.appendChild(d.createTextNode("[case " + caseV.name()));

      for (final var field : caseV.fields()) {
        final var a = d.createElementNS(XHTML, "a");
        a.setAttribute(
          "href",
          anchorPlus(var, "%s_%s".formatted(caseV.name(), field.name())));
        a.setTextContent(field.name());

        final var kw = d.createElementNS(XHTML, "span");
        kw.setAttribute("class", "cbKeyword");
        kw.setTextContent("field");

        final var e1 = d.createElementNS(XHTML, "div");
        e1.setAttribute("class", "cbTypeOverviewFieldLine");
        e1.appendChild(bracketOpen(d, '['));
        e1.appendChild(kw);
        e1.appendChild(a);
        e1.appendChild(processTypeExpression(d, pack, field.type()));
        e1.appendChild(bracketClose(d, ']', false));
        e0.appendChild(e1);
      }

      e0.appendChild(bracketClose(d, ']', false));
      e.appendChild(e0);
    }

    {
      final var e0 = d.createElementNS(XHTML, "div");
      e0.appendChild(bracketClose(d, ')', true));
      e.appendChild(e0);
    }

    return e;
  }

  private static Element processTypeExpression(
    final Document document,
    final CBPackageType currentPackage,
    final CBTypeExpressionType type)
  {
    if (type instanceof CBTypeExpressionApplication app) {
      final var e =
        document.createElementNS(XHTML, "span");
      e.appendChild(document.createTextNode("["));
      e.appendChild(processTypeExpression(
        document,
        currentPackage,
        app.target()));
      for (final var ex : app.arguments()) {
        e.appendChild(processTypeExpression(document, currentPackage, ex));
      }
      e.appendChild(document.createTextNode("]"));
      return e;
    }

    if (type instanceof CBTypeExprParameterType exprParam) {
      final var param = exprParam.parameter();
      final var e =
        document.createElementNS(XHTML, "a");
      e.setAttribute("href", anchorPlus(param.owner(), param.name()));
      e.setTextContent(param.name());
      return e;
    }

    if (type instanceof CBTypeExprNamedType named) {
      final var e = document.createElementNS(XHTML, "a");
      final var targetType = named.declaration();
      final var targetOwner = targetType.owner();
      if (Objects.equals(targetOwner, currentPackage)) {
        e.setAttribute("href", anchor(targetType));
      } else {
        final var file = targetOwner.name() + ".xhtml";
        e.setAttribute("href", file + anchor(targetType));
      }

      e.setTextContent(named.declaration().name());
      return e;
    }

    throw new IllegalStateException();
  }

  private Stream<CBPackageType> collectPackages(
    final CBPackageType p)
  {
    final var ip =
      p.imports()
        .stream()
        .flatMap(this::collectPackages);

    return Stream.concat(Stream.of(p), ip);
  }

  private record CBDocumentedType(
    CBTypeDeclarationType type,
    Element element)
  {

  }

  private record CBDocumentedProtocol(
    CBProtocolDeclarationType protocol,
    Element element)
  {

  }

  private record CBDocumentedPackage(
    Document document,
    Element bodyMain,
    String name,
    Map<String, CBDocumentedProtocol> protocols,
    Map<String, CBDocumentedType> types)
  {

  }

  private static String id(
    final CBTypeDeclarationType type)
  {
    return String.format("id_%s", type.id());
  }

  private static String anchor(
    final CBTypeDeclarationType type)
  {
    return String.format("#%s", id(type));
  }

  private static String idPlus(
    final CBTypeDeclarationType type,
    final String name)
  {
    return String.format("id_%s_%s", type.id(), name);
  }

  private static String anchorPlus(
    final CBTypeDeclarationType type,
    final String name)
  {
    return String.format("#%s", idPlus(type, name));
  }

  private static String id(
    final CBProtocolDeclarationType type)
  {
    return String.format("id_%s", type.id());
  }

  private static String anchor(
    final CBProtocolDeclarationType type)
  {
    return String.format("#%s", id(type));
  }

  private static String idPlus(
    final CBProtocolDeclarationType type,
    final String name)
  {
    return String.format("id_%s_%s", type.id(), name);
  }

  private static String anchorPlus(
    final CBProtocolDeclarationType type,
    final String name)
  {
    return String.format("#%s", idPlus(type, name));
  }
}
