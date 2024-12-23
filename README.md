# dnskotlin
[![Version](https://img.shields.io/maven-central/v/dev.sitar/dnskotlin)](https://search.maven.org/artifact/dev.sitar/dnskotlin)

An intuitive Kotlin Multiplatform DNS client.

## Dependency
`dnskotlin` releases are published to Maven Central (soon). Replace `{version}` with the latest version of `dnskotlin`.
```kotlin
implementation("dev.sitar:dnskotlin:{version}")
```

## What?
There are many DNS records to be resolved, and many platforms that they need to be resolved on. This library is a client implementation of the [DNS protocol](https://www.ietf.org/rfc/rfc1035.txt). 

Supports HTTPS (DoH) and UDP transports.

Supports the JVM, Linux, and Windows platforms.

Further implementation of the protocol, or support for additional platforms are added when needed (make an issue if something you need is missing).

## Example
```kotlin
// default global dns resolver (uses udp)
val dns = Dns
// or your own resolver with any supported transport.
val dns = dohDns(request = DohRequest.Post)

// retrieve mx records
val servers = dns.resolveRecursively("gmail.com") { qType = ResourceType.MX }
    .data.filterIsInstance<MXResourceData>()
    .sortedBy { it.preference }
    .map { it.exchange }
println(servers) // [gmail-smtp-in.l.google.com, alt1.gmail-smtp-in.l.google.com, alt2.gmail-smtp-in.l.google.com, alt3.gmail-smtp-in.l.google.com, alt4.gmail-smtp-in.l.google.com]
```