package dev.sitar.dns.transports

import dev.sitar.dns.Message
import io.ktor.network.sockets.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer

public class CommonUdpDnsTransport(public val socket: BoundDatagramSocket, public val timeout: Long) : DnsTransport {
    public override suspend fun send(to: DnsServer, message: Message): Message? {
        val source = Buffer().apply { Message.Factory.marshall(this, message) }
        socket.send(Datagram(source, InetSocketAddress(to.host, to.port)))

        val msg = withTimeoutOrNull(timeout) {
            Message.Factory.unmarshall(socket.receive().packet)
        }

        if (msg != null) {
            // request/response matching should probably be done for udp. doesn't seem to cause an issue though.
            require(msg.header.id == message.header.id)

            // expect response
            require(msg.header.qr)
        }

        return msg
    }
}