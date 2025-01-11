package dev.sitar.dns.dnssec

import dev.sitar.dns.Dns
import dev.sitar.dns.MessageBuilder
import dev.sitar.dns.crypto.Crypto
import dev.sitar.dns.proto.Message
import dev.sitar.dns.proto.records.ResourceRecord
import dev.sitar.dns.proto.records.ResourceType
import dev.sitar.dns.proto.records.data.DNSKEYResourceData
import dev.sitar.dns.proto.records.data.RRSIGResourceData
import dev.sitar.dns.transports.DnsServer
import dev.sitar.dns.transports.DnsTransport
import dev.sitar.dns.transports.send
import io.ktor.util.*
import kiso.common.logger
import kiso.log.debug
import kiso.log.trace
import kiso.log.warn
import kotlinx.datetime.Clock
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

private val LOG = logger("dnssec")

public data class ValidatedMessage(
    val isValid: Boolean,
    val data: VerificationData?,
    val message: Message
)

public data class VerificationData(
    val rrSet: String,
    val signature: String,
    val key: String,
)

public class ValidatingDns(private val crypto: Crypto, transport: DnsTransport, defaultServers: List<DnsServer>) :
    Dns(transport, defaultServers) {
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

    private fun verify_set_try_keys(
        sig: ResourceRecord<RRSIGResourceData>,
        set: List<ResourceRecord<*>>,
        keys: List<ResourceRecord<DNSKEYResourceData>>
    ): VerificationData? {
        if (!sig.data.is_valid()) {
            return null
        }

        // recreate data to verify
        val records = set
            .map { it.copy(name = it.name.lowercase(), ttl = sig.data.originalTtl) }
            .sortedWith(compareBy({ it.name }, { it.type.value }, { it.`class`.value }))

        val blob = Buffer()
        sig.data.copy(signature = byteArrayOf()).marshall(blob)
        records.forEach { ResourceRecord.marshall(blob, it) }

        blob.skip(2) // dont need the length, just rrdata

        val d = blob.readByteArray()

        LOG.trace { "computed data to be signed: ${d.encodeBase64()} " }

        // attempt to verify with each key.
        keys.forEach { key ->
            if (key.data.algorithm != sig.data.algorithm) {
                LOG.debug { "sig and key algo mismatch!" }
                return@forEach
            }

            if (sig.data.signerName != key.name) {
                LOG.debug { "sig and key signer mismatch!" }
                return@forEach
            }

            LOG.debug { "verifying using $key" }

            val is_valid = when (sig.data.algorithm) {
                DnssecAlgorithm.RsaSha256 -> crypto.rsaSha256Verify(d, sig.data.signature, key.data.publicKey)
                DnssecAlgorithm.EcdsaSha256 -> crypto.ecdsaSha256Verify(d, sig.data.signature, key.data.publicKey)
                else -> error("unsupported algorithm ${sig.data.algorithm}")
            }

            if (is_valid) {
                return VerificationData(
                    d.encodeBase64(),
                    sig.data.signature.encodeBase64(),
                    key.data.publicKey.encodeBase64(),
                )
            }
        }

        return null
    }

    public suspend fun validate(message: Message): ValidatedMessage {
        fun not_valid(data: VerificationData? = null) = ValidatedMessage(false, data, message)

        val question = message.questions.firstOrNull() ?: return not_valid()

        val name = question.qName
        val type = question.qType

        LOG.debug { "attempting to validate rr set of ${message.header.id} over type $type." }

        val sigs = message.answers.filter { it.type == ResourceType.RRSIG }
            .filterIsInstance<ResourceRecord<RRSIGResourceData>>()
            .filter { it.data.type == type }
            .filter { it.data.algorithm.is_supported }

        if (sigs.isEmpty()) {
            LOG.warn { "no supported signature set found!" }
            return not_valid()
        }

        LOG.debug { "found rrset signatures $sigs" }

        // retrieve public key used to verify
        LOG.debug { "retrieving dns key for $name." }
        val key_rx = resolveRecursively { question(name) { qType = ResourceType.DNSKEY } }
        LOG.debug { "obtained $key_rx" }

        val keys = key_rx?.answers.orEmpty()
            .filter { it.type == ResourceType.DNSKEY }
            .filterIsInstance<ResourceRecord<DNSKEYResourceData>>()
            .filter { it.data.flags.zoneKey }
            .filter { !it.data.flags.secureEntryPoint } // secure entry point is for trust anchors. e.g., validating rr set over type DNSKEY

        if (keys.isEmpty()) {
            LOG.warn { "could not retrieve dns key to verify with for $name!" }
            return not_valid()
        }

        val record_set = message.answers.filter { it.type == type }

        sigs.forEach {
            val verification = verify_set_try_keys(it, record_set, keys)

            if (verification != null) return ValidatedMessage(true, verification, message)
        }

        return not_valid()
    }
}

public fun Dns.validating(crypto: Crypto): ValidatingDns {
    return ValidatingDns(crypto, transport, defaultServers)
}

private fun RRSIGResourceData.is_valid(): Boolean {
    val now = Clock.System.now().epochSeconds

    return now < expiration && now > inception
}

private val DnssecAlgorithm.is_supported: Boolean
    get() = when (this) {
        DnssecAlgorithm.RsaSha256 -> true
        DnssecAlgorithm.EcdsaSha256 -> true
        else -> false
    }
