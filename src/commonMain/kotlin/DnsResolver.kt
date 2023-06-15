package dev.sitar.dns

import dev.sitar.dns.records.NSResourceRecord
import dev.sitar.dns.records.ResourceRecord
import dev.sitar.dns.transports.CommonUdpDnsTransport
import dev.sitar.dns.transports.DnsTransport
import dev.sitar.dns.transports.DnsServer
import dev.sitar.dns.transports.send
import io.ktor.network.selector.*
import io.ktor.network.sockets.*

public val ROOT_NAME_SERVERS: List<DnsServer> = listOf(
    "a.root-servers.net",
    "b.root-servers.net",
    "c.root-servers.net",
    "d.root-servers.net",
    "e.root-servers.net",
    "f.root-servers.net",
    "g.root-servers.net",
    "h.root-servers.net",
    "i.root-servers.net",
    "j.root-servers.net",
    "k.root-servers.net",
    "l.root-servers.net",
    "m.root-servers.net",
).map { DnsServer(it) }

public sealed interface MessageResponse {
    public class NameServers(public val nameServers: List<DnsServer>) : MessageResponse
    public class Answers(public val answers: List<ResourceRecord>) : MessageResponse
}

public class DnsResolver(public val transport: DnsTransport) {
    public suspend fun resolve(
        host: String,
        nameServers: List<DnsServer> = ROOT_NAME_SERVERS,
        block: QuestionBuilder.() -> Unit = { }
    ): MessageResponse? {
        for (root in nameServers) {
            val response = transport.send(root) {
                question(host, block)
            } ?: continue

            when {
                response.answers.isNotEmpty() -> {
                    return MessageResponse.Answers(response.answers)
                }

                response.authoritativeRecords.isNotEmpty() -> {
                    return MessageResponse.NameServers(response.authoritativeRecords.filterIsInstance<NSResourceRecord>().map { DnsServer(it.data.nameServer, 53) })
                }
            }
        }

        return null
    }

    public suspend fun resolveRecursively(
        host: String,
        roots: List<DnsServer> = ROOT_NAME_SERVERS,
        block: QuestionBuilder.() -> Unit = { }
    ): List<ResourceRecord>? {
        var servers = roots

        while (servers.isNotEmpty()) {
            when (val response = resolve(host, servers, block)) {
                is MessageResponse.Answers -> {
                    return response.answers
                }

                is MessageResponse.NameServers -> {
                    servers = response.nameServers
                }

                null -> return null
            }
        }

        return null
    }
}

public fun dnsResolver(): DnsResolver {
    return DnsResolver(CommonUdpDnsTransport(aSocket(SelectorManager()).udp().bind(), 100))
}