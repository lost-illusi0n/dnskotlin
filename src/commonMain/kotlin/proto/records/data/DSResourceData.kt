package dev.sitar.dns.proto.records.data

import dev.sitar.dns.dnssec.DnssecAlgorithm
import dev.sitar.dns.dnssec.DnssecDigest
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

public class DSResourceData(
    public val tag: Short,
    public val algorithm: DnssecAlgorithm,
    public val digestType: DnssecDigest,
    public val digest: ByteArray
) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: DSResourceData) {
            output.writeShort((4 + data.digest.size).toShort())

            output.writeShort(data.tag)
            output.writeByte(data.algorithm.value)
            output.writeByte(data.digestType.value)
            output.write(data.digest)
        }

        public fun unmarshall(input: Source): DSResourceData {
            val len = input.readShort()

            val tag = input.readShort()
            val algorithm = DnssecAlgorithm.fromValue(input.readByte())
            val digestType = DnssecDigest.fromValue(input.readByte())
            val digestLength = len - 4
            val digest = input.readByteArray(digestLength)

            return DSResourceData(tag, algorithm, digestType, digest)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}