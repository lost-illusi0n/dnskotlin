package dev.sitar.dns.transports

import dev.sitar.dns.proto.Message
import io.ktor.network.sockets.*
import kiso.common.logger
import kiso.log.error
import kiso.log.trace
import kiso.log.warn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer

private val LOG = logger("transport(udp)")

public class CommonUdpDnsTransport(
    public val socket: BoundDatagramSocket, public val timeout: Long,
    override val preferredPort: Int = TRANSPORT_DNS_PORT
) : DnsTransport {
    public override suspend fun send(to: DnsServer, message: Message): Message? {
        val source = Buffer().apply { Message.Factory.marshall(this, message) }
        socket.send(Datagram(source, InetSocketAddress(to.host, to.port)))

        LOG.trace { "tx id ${message.header.id}." }

        val msg = withTimeoutOrNull(timeout) {
            try {
                Message.Factory.unmarshall(socket.receive().packet)
            } catch (e: Exception) {
                LOG.error(e) { "failed to parse response for id ${message.header.id}." }
                null
            }
        }

        if (msg != null) {
            // request/response matching should probably be done for udp. doesn't seem to cause an issue though.
            require(msg.header.id == message.header.id)

            // expect response
            require(msg.header.qr)

            LOG.trace { "rx id ${msg.header.id}." }
        } else {
            LOG.warn { "missing rx for id ${message.header.id}." }
        }

        return msg
    }
}