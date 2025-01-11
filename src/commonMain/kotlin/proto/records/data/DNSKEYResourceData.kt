package dev.sitar.dns.proto.records.data

import dev.sitar.dns.dnssec.DnssecAlgorithm
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlin.experimental.and
import kotlin.experimental.or

public data class DnsKeyFlags(
    val zoneKey: Boolean,
    val secureEntryPoint: Boolean
) {
    public companion object {
        public const val ZONE_KEY_MASK: Short = (1 shl 8).toShort()
        public const val SECURE_ENTRY_POINT_MASK: Short = (1 shl 0).toShort()

        public fun fromShort(flags: Short): DnsKeyFlags {
            val zoneKey = flags and ZONE_KEY_MASK == ZONE_KEY_MASK
            val secureEntryPoint = flags and SECURE_ENTRY_POINT_MASK == SECURE_ENTRY_POINT_MASK

            return DnsKeyFlags(zoneKey, secureEntryPoint)
        }
    }

    public fun toShort(): Short {
        var flags = 0.toShort()

        if (zoneKey) flags = flags or SECURE_ENTRY_POINT_MASK
        if (secureEntryPoint) flags = flags or ZONE_KEY_MASK

        return flags
    }
}

public data class DNSKEYResourceData(
    public val flags: DnsKeyFlags,
    public val algorithm: DnssecAlgorithm,
    public val publicKey: ByteArray
) : ResourceData() {
    public companion object {
        public const val PROTOCOL: Byte = 3

        public fun marshall(output: Sink, data: DNSKEYResourceData) {
            output.writeShort((4 + data.publicKey.size).toShort())
            output.writeShort(data.flags.toShort())
            output.writeByte(PROTOCOL)
            output.writeByte(data.algorithm.value)
            output.write(data.publicKey)
        }

        public fun unmarshall(input: Source): DNSKEYResourceData {
            val length = input.readShort()

            val flags = DnsKeyFlags.fromShort(input.readShort())

            require(input.readByte() == PROTOCOL)

            val algorithm = DnssecAlgorithm.fromValue(input.readByte())

            val keyLength = length - 4
            val key = input.readByteArray(keyLength)

            return DNSKEYResourceData(flags, algorithm, key)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}