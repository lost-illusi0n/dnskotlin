package dev.sitar.dns

import dev.sitar.dns.records.ResourceRecord
import dev.sitar.dns.records.data.NSResourceData
import dev.sitar.dns.records.data.ResourceData
import dev.sitar.dns.transports.CommonUdpDnsTransport
import dev.sitar.dns.transports.DnsServer
import dev.sitar.dns.transports.DnsTransport
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
    public class Answers(public val answers: List<ResourceRecord<*>>) : MessageResponse
}

public open class Dns(
    public val transport: DnsTransport = CommonUdpDnsTransport(aSocket(SelectorManager()).udp().bind(), 100),
    public val defaultServers: List<DnsServer> = ROOT_NAME_SERVERS
) {
    public companion object Default : Dns()

    public suspend fun resolve(
        host: String,
        nameServers: List<DnsServer> = defaultServers,
        block: QuestionBuilder.() -> Unit = { }
    ): MessageResponse? {
        for (server in nameServers) {
            val response = transport.send(server) {
                question(host, block)
            } ?: continue

            when {
                response.answers.isNotEmpty() -> {
                    return MessageResponse.Answers(response.answers)
                }

                response.authoritativeRecords.isNotEmpty() -> {
                    val servers = response.authoritativeRecords
                        .filterIsInstance<ResourceRecord<NSResourceData>>()
                        .map { DnsServer(it.data.nameServer) }

                    return MessageResponse.NameServers(servers)
                }
            }
        }

        return null
    }

    public suspend fun resolveRecursively(
        host: String,
        roots: List<DnsServer> = defaultServers,
        block: QuestionBuilder.() -> Unit = { }
    ): List<ResourceRecord<*>> {
        for (root in roots) {
            var servers = roots

            while (servers.isNotEmpty()) {
                when (val response = resolve(host, servers, block)) {
                    is MessageResponse.Answers -> {
                        return response.answers
                    }

                    is MessageResponse.NameServers -> {
                        servers = response.nameServers
                    }

                    null -> return emptyList()
                }
            }
        }

        return emptyList()
    }
}

public val List<ResourceRecord<*>>.data: List<ResourceData> get() = map { it.data }