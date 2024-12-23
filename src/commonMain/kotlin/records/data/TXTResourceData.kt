package dev.sitar.dns.records.data

import dev.sitar.dns.MessageReadScope
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

public data class TXTResourceData(public val txts: List<String>) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: TXTResourceData) {
            output.writeShort(data.txts.sumOf { it.length }.toShort())
            data.txts.forEach {
                output.writeByte(it.length.toByte())
                output.write(it.encodeToByteArray())
            }
        }

        public fun unmarshall(input: Source): TXTResourceData {
            val total = input.readShort() - 1

            var current = 0

            val txts = buildList {
                while (current < total) {
                    val len = input.readByte().toInt()
                    add(input.readByteArray(len).decodeToString())
                    current += len
                }
            }

            return TXTResourceData(txts)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}