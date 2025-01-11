# dnskotlin
[![Version](https://img.shields.io/maven-central/v/dev.sitar/dnskotlin)](https://search.maven.org/artifact/dev.sitar/dnskotlin)

An intuitive Kotlin Multiplatform DNS client.

## Dependency
`dnskotlin` releases are published to Maven Central. Replace `{version}` with the latest version of `dnskotlin`.
```kotlin
implementation("dev.sitar:dnskotlin:{version}")
```

## What?
There are many DNS records to be resolved, and many platforms that they need to be resolved on. This library is a client implementation of the [DNS protocol](https://www.ietf.org/rfc/rfc1035.txt). 

Supports HTTPS (DoH) and UDP transports.

Supports the JVM, Linux, and Windows platforms.

Partial DNSSEC support (see below).

Further implementation of the protocol, or support for additional platforms may be added by request.

## Guide
### Transports
Specify the transport used with `dev.sitar.dns.dohResolver` (HTTPS) and `dev.sitar.dns.udpResolver` (UDP). Alternatively, use the default DNS object that uses a UDP transport.
```kotlin
// default global dns resolver (you can forgo the variable in this case and just use Dns)
val dns = Dns

// your own resolver. this one is doh using post requests
val dns = dohDns(request = DohRequest.Post)

// your own resolver. this one is udp
val dns = udpDns()
```

### Messages
Every DNS instance supports sending a DNS message through `resolve` or `resolveRecursively`. The former simply sends the message and returns a response, while the latter will recursively send the message until it finds the proper server that will handle the question.

```kotlin
// retrieve mx records
val servers = dns.resolveRecursively("gmail.com") { qType = ResourceType.MX }
    .data.filterIsInstance<MXResourceData>()
    .sortedBy { it.preference }
    .map { it.exchange }

println(servers) // [gmail-smtp-in.l.google.com, alt1.gmail-smtp-in.l.google.com, alt2.gmail-smtp-in.l.google.com, alt3.gmail-smtp-in.l.google.com, alt4.gmail-smtp-in.l.google.com]
```

### DNSSEC
DNSSEC validation is partially supported out-of-the box on the JVM. Native platforms must implement the `dev.sitar.dns.crypto.Crypto` interface for now. When requested, dnskotlin will enable DNSSEC capability for outgoing messages and attempt to verify the validity of the responses. Currently, it will only verify the response's RRset. Verifying the full chain of trust is planned. A resolver with this capability is obtained by calling `.validating(cryptoImpl)` on it (or just `.validating()` on supported platforms).

```kotlin
// on the jvm, there is a built-in crypto impl. no need to specify.
val dns = dohDns(request = DohRequest.Post).validating()

val validation = dns.validateRecursively("sitar.dev") { qType = ResourceType.A }
require(validation.isValid)
require(validation.data) // verification data
println(validation.message)
```
