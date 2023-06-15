package dev.sitar.dns.transports

import dev.sitar.dns.Message
import dev.sitar.kio.Slice
import dev.sitar.kio.buffers.asBuffer
import dev.sitar.kio.buffers.buffer
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.withTimeoutOrNull

public class CommonUdpDnsTransport(public val socket: BoundDatagramSocket, public val timeout: Long) : DnsTransport {
    public override suspend fun send(to: DnsServer, message: Message): Message? {
        val data = buffer { Message.Factory.marshall(this, message) }
        val packet = data.fullSlice().toByteReadPacket()
        socket.send(Datagram(packet, InetSocketAddress(to.host, to.port)))

        return withTimeoutOrNull(timeout) {
            Message.Factory.unmarshall(socket.receive().packet.readBytes().asBuffer())
        }
    }
}

private fun Slice.toByteReadPacket(): ByteReadPacket {
    return ByteReadPacket(bytes, start, length)
}