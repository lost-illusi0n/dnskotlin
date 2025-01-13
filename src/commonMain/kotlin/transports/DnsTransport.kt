package dev.sitar.dns.transports

import dev.sitar.dns.MessageBuilder
import dev.sitar.dns.proto.Message

public interface DnsTransport {
    public val preferredPort: Int
    public val isSecure: Boolean

    public suspend fun send(to: DnsServer, message: Message): Message?
}

public suspend fun DnsTransport.send(to: DnsServer, messageBuilder: MessageBuilder.() -> Unit): Message? {
    return send(to, MessageBuilder().also(messageBuilder).build())
}