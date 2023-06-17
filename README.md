# dnskotlin
[![Version](https://img.shields.io/maven-central/v/dev.sitar/dnskotlin)](https://search.maven.org/artifact/dev.sitar/dnskotlin)

A Kotlin Multiplatform DNS client.

## Dependency
`dnskotlin` releases are published to Maven Central (soon). Replace `{version}` with the latest version of `dnskotlin`.
```kotlin
implementation("dev.sitar:dnskotlin:{version}")
```

## What?
There are many DNS records to be resolved, and many platforms that they need to be resolved on. This library provides a basic client implementation for the [DNS protocol](https://www.ietf.org/rfc/rfc1035.txt). 

Further implementation of the protocol, or support for additional platforms are added when needed (make an issue if something you need is missing).
