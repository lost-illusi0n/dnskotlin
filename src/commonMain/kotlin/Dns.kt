package dev.sitar.dns

import dev.sitar.dns.proto.Message
import dev.sitar.dns.proto.records.ResourceRecord
import dev.sitar.dns.proto.records.data.NSResourceData
import dev.sitar.dns.proto.records.data.ResourceData
import dev.sitar.dns.transports.*
import io.ktor.client.*
import io.ktor.client.engine.*
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

public open class Dns(
    public val transport: DnsTransport,
    public val defaultServers: List<DnsServer>
) {
    public companion object Default :
        Dns(
            CommonUdpDnsTransport(DEFAULT_UDP_SOCKET, DEFAULT_TIMEOUT),
            defaultServers = ROOT_NAME_SERVERS.map { DnsServer(it, TRANSPORT_DNS_PORT) }
        )

    public open suspend fun resolve(
        server: DnsServer = defaultServers.first(),
        block: MessageBuilder.() -> Unit = { }
    ): Message? {
        return transport.send(server, block)
    }

    public suspend fun resolveRecursively(
        roots: List<DnsServer> = defaultServers,
        block: MessageBuilder.() -> Unit = { }
    ): Message? {
        var servers = roots

        while (servers.isNotEmpty()) {
            val rsp = servers.firstNotNullOfOrNull { resolve(it, block) } ?: break

            if (rsp.answers.isNotEmpty()) return rsp

            if (rsp.authoritativeRecords.isNotEmpty()) {
                servers = rsp.authoritativeRecords.data
                    .filterIsInstance<NSResourceData>()
                    .map { DnsServer(it.nameServer, transport.preferredPort) }

                continue
            }

            return null
        }

        return null
    }

    public suspend fun resolve(
        host: String,
        server: DnsServer = defaultServers.first(),
        block: QuestionBuilder.() -> Unit = { }
    ): List<ResourceRecord<*>> = resolve(server) { question(host, block) }?.answers.orEmpty()

    public suspend fun resolveRecursively(
        host: String,
        roots: List<DnsServer> = defaultServers,
        block: QuestionBuilder.() -> Unit = { }
    ): List<ResourceRecord<*>> = resolveRecursively(roots) { question(host, block) }?.answers.orEmpty()
}

private const val DEFAULT_TIMEOUT: Long = 1000

internal expect val DEFAULT_ENGINE: HttpClientEngine
private val DEFAULT_CLIENT = HttpClient(DEFAULT_ENGINE)

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