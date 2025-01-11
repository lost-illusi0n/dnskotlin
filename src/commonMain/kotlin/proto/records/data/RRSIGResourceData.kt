package dev.sitar.dns.proto.records.data

import dev.sitar.dns.dnssec.DnssecAlgorithm
import dev.sitar.dns.proto.MessageReadScope
import dev.sitar.dns.proto.records.ResourceType
import dev.sitar.dns.proto.records.decompressName
import dev.sitar.dns.proto.records.dnsNameByteLength
import dev.sitar.dns.proto.records.writeName
import kotlinx.io.Sink
import kotlinx.io.readByteArray

public data class RRSIGResourceData(
    public val type: ResourceType,
    public val algorithm: DnssecAlgorithm,
    public val labels: Byte,
    public val originalTtl: Int,
    public val expiration: Int, // serial number arithmetic?
    public val inception: Int,
    public val tag: Short,
    public val signerName: String,
    public val signature: ByteArray
) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: RRSIGResourceData) {
            val length = 18 + data.signerName.dnsNameByteLength + data.signature.size

            output.writeShort(length.toShort())
            output.writeShort(data.type.value)
            output.writeByte(data.algorithm.value)
            output.writeByte(data.labels)
            output.writeInt(data.originalTtl)
            output.writeInt(data.expiration)
            output.writeInt(data.inception)
            output.writeShort(data.tag)
            writeName(data.signerName, output)
            output.write(data.signature)
        }

        public fun unmarshall(scope: MessageReadScope): RRSIGResourceData = scope {
            val len = input.readShort()

            val type = ResourceType.fromValue(input.readShort())
            val algorithm = DnssecAlgorithm.fromValue(input.readByte())
            val labels = input.readByte()
            val ttl = input.readInt()
            val expiration = input.readInt()
            val inception = input.readInt()
            val tag = input.readShort()
            // we can ignore passing a new read scope since this field should never be compressed.
            val signerName = decompressName(this)
            val signatureLength = len - 18 - (signerName.dnsNameByteLength)
            val signature = input.readByteArray(signatureLength)

            return RRSIGResourceData(type, algorithm, labels, ttl, expiration, inception, tag, signerName, signature)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}