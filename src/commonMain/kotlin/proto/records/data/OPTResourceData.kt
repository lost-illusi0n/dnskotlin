package dev.sitar.dns.proto.records.data

import dev.sitar.dns.proto.records.ResourceRecord
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

public class OptOption(public val code: Short, public val data: ByteArray)

public typealias OPTResourceRecord = ResourceRecord<OPTResourceData>

public val OPTResourceRecord.extendedRCode: Byte get() = ttl.shr(8).toByte()
public val OPTResourceRecord.flags: Byte get() = (ttl and 0xFF).toByte()
public val OPTResourceRecord.dnssecOk: Boolean get() = (flags.toInt() and 0xFF).shr(7) == 1

public data class OPTResourceData(public val options: List<OptOption>) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: OPTResourceData) {
            val optLen = data.options.sumOf { 2 + it.data.size }.toShort()

            output.writeShort(optLen)

            for (opt in data.options) {
                output.writeShort(opt.code)
                output.write(opt.data)
            }
        }

        public fun unmarshall(input: Source): OPTResourceData {
            var remaining = input.readShort()

            val opts = buildList {
                while (remaining > 0) {
                    val code = input.readShort()
                    val optLength = input.readShort().toInt()
                    val opt = input.readByteArray(optLength)

                    add(OptOption(code, opt))

                    remaining = (remaining - optLength - 4).toShort()
                }
            }

            return OPTResourceData(opts)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}