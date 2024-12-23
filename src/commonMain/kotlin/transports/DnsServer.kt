package dev.sitar.dns.transports

public const val TRANSPORT_DNS_PORT: Int = 53       // udp/tcp
public const val APPLICATION_DNS_PORT: Int = 443    // https

public data class DnsServer(val host: String, val port: Int)