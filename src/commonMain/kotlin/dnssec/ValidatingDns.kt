package dev.sitar.dns.dnssec

import dev.sitar.dns.Dns
import dev.sitar.dns.MessageBuilder
import dev.sitar.dns.crypto.Crypto
import dev.sitar.dns.proto.Message
import dev.sitar.dns.transports.DnsServer
import dev.sitar.dns.transports.DnsTransport
import dev.sitar.dns.transports.send
import kiso.common.logger
import kiso.log.debug
import kiso.log.warn

private val LOG = logger("dnssec")

public data class ValidatedMessage(
    val isValid: Boolean,
    val strategy: ValidationStrategy?,
    val data: VerificationData?,
    val message: Message
)

public data class VerificationData(
    val rrSet: String,
    val signature: String,
    val key: String,
)

public class ValidatingDns(
    private val crypto: Crypto,
    private val strategy: ValidationStrategy,
    transport: DnsTransport,
    defaultServers: List<DnsServer>
) : Dns(transport, defaultServers) {
    init {
        if (strategy is ValidationStrategy.AuthenticatedDataFlag && !transport.isSecure) {
            LOG.warn { "Only checking for the authenticated data flag, when the underlying transport is not secure, is vulnerable to MITM attacks and may not even work! It may be favorable to use a secure transport like DoH, and or another validation strategy." }
        }
    }

    override suspend fun resolve(server: DnsServer, block: MessageBuilder.() -> Unit): Message? {
        return transport.send(server) { block(); options { dnssecOk = true } }
    }

    public suspend fun validate(
        server: DnsServer = defaultServers.first(),
        block: MessageBuilder.() -> Unit
    ): ValidatedMessage? {
        val msg = resolve(server, block) ?: return null

        return validate(msg)
    }

    public suspend fun validateRecursively(
        servers: List<DnsServer> = defaultServers,
        block: MessageBuilder.() -> Unit
    ): ValidatedMessage? {
        val msg = resolveRecursively(servers, block) ?: return null

        return validate(msg)
    }
    public suspend fun validate(message: Message): ValidatedMessage {
        strategy.as_list().forEach {
            LOG.debug { "validating using $it" }

            when (it) {
                is ValidationStrategy.AuthenticatedDataFlag -> if (message.header.ad) return ValidatedMessage(true, it, null, message)
                is ValidationStrategy.VerifySet -> {
                    val data = it.verify(this, crypto, message)
                    if (data != null) return ValidatedMessage(true, it, data, message)
                }
            }
        }

        return ValidatedMessage(false, null, null, message)
    }
}

public fun Dns.validating(crypto: Crypto, strategy: ValidationStrategy): ValidatingDns {
    return ValidatingDns(crypto, strategy, transport, defaultServers)
}


// DEFAULT STRATEGY
public fun Dns.validating(crypto: Crypto): ValidatingDns {
    val strategy = if (transport.isSecure) ValidationStrategy.AuthenticatedDataFlag else ValidationStrategy.VerifySet
    return ValidatingDns(crypto, strategy, transport, defaultServers)
}

private fun ValidationStrategy.as_list(): List<ValidationStrategy.Specific> {
    return when (this) {
        is ValidationStrategy.Ordered -> this.strategies
        is ValidationStrategy.Specific -> listOf(this)
    }
}