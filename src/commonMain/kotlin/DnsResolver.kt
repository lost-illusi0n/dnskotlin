package dev.sitar.dns

import dev.sitar.dns.records.ResourceRecord
import dev.sitar.kio.buffers.asBuffer
import dev.sitar.kio.buffers.buffer
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.withTimeoutOrNull

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
    public class NameServers(public val nameServers: List<String>) : MessageResponse
    public class Answers(public val answers: List<ResourceRecord>) : MessageResponse
}

public class DnsResolver(public val socket: BoundDatagramSocket) {
    public suspend fun resolve(
        host: String,
        nameServers: List<String> = ROOT_NAME_SERVERS,
        timeout: Long = 100,
        block: QuestionBuilder.() -> Unit = { }
    ): MessageResponse? {
        val queryMessage = message { question(host, block) }

        val data = buffer { Message.Factory.marshall(this, queryMessage) }
        val (arr, off, len) = data.fullSlice()
        val packet = ByteReadPacket(arr, off, len)

        for (root in nameServers) {
            socket.send(Datagram(packet.copy(), InetSocketAddress(root, 53)))

            val response = withTimeoutOrNull(timeout) {
                Message.Factory.unmarshall(socket.receive().packet.readBytes().asBuffer())
            } ?: continue

            when {
                response.answers.isNotEmpty() -> {
                    return MessageResponse.Answers(response.answers)
                }

                response.nameServers.isNotEmpty() -> {
                    return MessageResponse.NameServers(response.nameServers.map { it.data.nameServer })
                }
            }
        }

        return null
    }

    public suspend fun resolveRecursively(
        host: String,
        roots: List<String> = ROOT_NAME_SERVERS,
        timeout: Long = 100,
        block: QuestionBuilder.() -> Unit = { }
    ): List<ResourceRecord>? {
        var servers = roots

        while (true) {
            when (val response = resolve(host, servers, timeout, block)) {
                is MessageResponse.Answers -> {
                    return response.answers
                }

                is MessageResponse.NameServers -> {
                    servers = response.nameServers
                }

                null -> return null
            }
        }
    }
}

public fun dnsResolver(): DnsResolver {
    return DnsResolver(aSocket(SelectorManager()).udp().bind())
}