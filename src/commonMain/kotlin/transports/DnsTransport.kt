package dev.sitar.dns.transports

import dev.sitar.dns.Message
import dev.sitar.dns.MessageBuilder

public interface DnsTransport {
    public suspend fun send(to: DnsServer, message: Message): Message?
}

public suspend fun DnsTransport.send(to: DnsServer, messageBuilder: MessageBuilder.() -> Unit): Message? {
    return send(to, MessageBuilder().also(messageBuilder).build())
}