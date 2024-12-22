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

## Example
```kotlin
// default global dns resolver
val dns = Dns
// or your own resolver
val dns = Dns(defaultServers = listOf(DnsServer("8.8.8.8")))

// retrieve mx records
val servers = dns.resolveRecursively("gmail.com") { qType = ResourceType.MX }
    .data.filterIsInstance<MXResourceData>()
    .sortedBy { it.preference }
    .map { it.exchange }
println(servers) // [gmail-smtp-in.l.google.com, alt1.gmail-smtp-in.l.google.com, alt2.gmail-smtp-in.l.google.com, alt3.gmail-smtp-in.l.google.com, alt4.gmail-smtp-in.l.google.com]
```