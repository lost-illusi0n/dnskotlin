package dev.sitar.dns.transports

import dev.sitar.dns.Message
import dev.sitar.kio.Slice
import dev.sitar.kio.buffers.DefaultBufferPool
import dev.sitar.kio.buffers.asBuffer
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Source
import kotlinx.io.readByteArray

public class CommonUdpDnsTransport(public val socket: BoundDatagramSocket, public val timeout: Long) : DnsTransport {
    public override suspend fun send(to: DnsServer, message: Message): Message? {
        val data = DefaultBufferPool.acquire(12)
        Message.Factory.marshall(data, message)

        val packet = data.fullSlice().asSource()
        socket.send(Datagram(packet, InetSocketAddress(to.host, to.port)))

        DefaultBufferPool.recycle(data)

        val msg = withTimeoutOrNull(timeout) {
            Message.Factory.unmarshall(socket.receive().packet.readByteArray().asBuffer())
        }

        if (msg != null) {
            // request/response matching should probably be done. doesn't seem to cause an issue though.
            require(msg.header.id == message.header.id)

            // expect response
            require(msg.header.qr)
        }

        return msg
    }
}

internal fun Slice.asSource(): Source {
    return ByteReadPacket(bytes, start, length)
}