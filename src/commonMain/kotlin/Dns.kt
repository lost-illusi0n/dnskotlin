package dev.sitar.dns

import dev.sitar.dns.records.ResourceRecord
import dev.sitar.dns.records.data.NSResourceData
import dev.sitar.dns.records.data.ResourceData
import dev.sitar.dns.transports.*
import io.ktor.client.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.runBlocking

public val ROOT_NAME_SERVERS: List<String> = listOf(
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
)

public sealed interface MessageResponse {
    public class NameServers(public val nameServers: List<DnsServer>) : MessageResponse
    public class Answers(public val answers: List<ResourceRecord<*>>) : MessageResponse
}

public open class Dns(
    public val transport: DnsTransport,
    public val defaultServers: List<DnsServer>
) {
    public companion object Default :
        Dns(
            CommonUdpDnsTransport(DEFAULT_UDP_SOCKET, DEFAULT_TIMEOUT),
            defaultServers = ROOT_NAME_SERVERS.map { DnsServer(it, TRANSPORT_DNS_PORT) }
        )

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
                    val servers = response.authoritativeRecords.data
                        .filterIsInstance<NSResourceData>()
                        .map { DnsServer(it.nameServer, server.port) }

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

private const val DEFAULT_TIMEOUT: Long = 1000

private val DEFAULT_CLIENT = HttpClient()

public const val CLOUDFLARE_DOH_SERVER: String = "https://cloudflare-dns.com"

/**
 * @param servers DoH-compatible servers. They should include a scheme. e.g., https://cloudflare-dns.com.
 */
public fun dohDns(
    vararg servers: String = arrayOf(CLOUDFLARE_DOH_SERVER),
    client: HttpClient = DEFAULT_CLIENT,
    request: DohRequest = DohRequest.Post,
    timeout: Long = DEFAULT_TIMEOUT
): Dns {
    return Dns(
        CommonHttpDnsTransport(client, request, timeout),
        defaultServers = servers.map { DnsServer(it, APPLICATION_DNS_PORT) }
    )
}

private val DEFAULT_UDP_SOCKET = runBlocking { aSocket(SelectorManager()).udp().bind() }

public fun udpDns(
    vararg servers: String = ROOT_NAME_SERVERS.toTypedArray(),
    socket: BoundDatagramSocket = DEFAULT_UDP_SOCKET,
    timeout: Long = DEFAULT_TIMEOUT
): Dns {
    return Dns(
        CommonUdpDnsTransport(socket, timeout),
        defaultServers = servers.map { DnsServer(it, TRANSPORT_DNS_PORT) }
    )
}

public val List<ResourceRecord<*>>.data: List<ResourceData> get() = map { it.data }