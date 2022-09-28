# dnskotlin
[![Version](https://img.shields.io/maven-central/v/dev.sitar/kio)](https://search.maven.org/artifact/dev.sitar/dnskotlin)

A Kotlin Multiplatform DNS client.

## Dependency
`dnskotlin` releases are published to Maven Central. Replace `{version}` with the latest version of `dnskotlin`.
```kotlin
implementation("dev.sitar:dnskotlin:{version}")
```

## Quickstart
The main driver of this library is `DnsResolver`. It uses a UDP socket in order to transmit and receive DNS messages. It has a `resolve` and `resolveRecursive` function.
- `resolve`: will send your question to a list of DNS servers until it gets a response.
- `resolveRecursive`: will recursively send a question to the list of DNS servers you give it and then the nameservers it will receive as a response, over and over, until it gets an answer.