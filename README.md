cedarbridge
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.cedarbridge/com.io7m.cedarbridge.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.cedarbridge%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.cedarbridge/com.io7m.cedarbridge?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/cedarbridge/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/cedarbridge.svg?style=flat-square)](https://codecov.io/gh/io7m-com/cedarbridge)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.cedarbridge](./src/site/resources/cedarbridge.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/cedarbridge/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/cedarbridge/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/cedarbridge/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/cedarbridge/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/cedarbridge/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/cedarbridge/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/cedarbridge/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/cedarbridge/actions?query=workflow%3Amain.windows.temurin.lts)|

## cedarbridge

The `cedarbridge` package implements an efficient, strongly-typed,
platform-independent, binary message protocol language based around sum and
product types, and strong versioning.

```
[documentation ChatCommandJoin "Join the chat room."]
[record ChatCommandJoin
  [field name cb:String]
  [documentation name "The username with which to join."]]

[documentation Chat "The chat protocol."]
[protocol Chat
  [version 1
    [types-added
      ChatCommandJoin
      ChatCommandSpeak
      ChatEventJoined
      ChatEventLeft
      ChatEventSpoke
      ChatEventError]]]

...
```

## Features

* A carefully written [language specification](https://www.io7m.com/software/cedarbridge/specification/).
* A minimal data definition language based on algebraic sum and product types.
* Strong versioning as a core aspect of the language.
* A small, easily auditable codebase with a heavy use of modularity for correctness.
* Generates simple, readable, reflection-free Java code.
* A small footprint; the generated Java code consists of trivial record
  definitions and incredibly simple and straightforward code to serialize and
  deserialize values.
* Platform independence. No platform-dependent code is included in any form.
* Suitable for use in security-critical environments: Serialization code is
  immune to the usual serialization vulnerabilities such as type confusion,
  resource exhaustion, exponential parse times, and etc.
* An extensive automated test suite with high coverage.
* [OSGi-ready](https://www.osgi.org/).
* [JPMS-ready](https://en.wikipedia.org/wiki/Java_Platform_Module_System).
* ISC license.

## Usage

See the [documentation](https://www.io7m.com/software/cedarbridge) and
[language specification](https://www.io7m.com/software/cedarbridge).

