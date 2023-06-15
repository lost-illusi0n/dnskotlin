package dev.sitar.dns.transports

public const val DEFAULT_DNS_PORT: Int = 53

public data class DnsServer(val host: String, val port: Int = DEFAULT_DNS_PORT)