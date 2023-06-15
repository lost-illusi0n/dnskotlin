package dev.sitar.dns.records.data

import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.readBytes
import dev.sitar.kio.buffers.writeBytes

public data class TXTResourceData(public val txts: List<String>) : ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: TXTResourceData) {
            output.writeShort(data.txts.sumOf { it.length }.toShort())
            data.txts.forEach {
                output.write(it.length.toByte())
                output.writeBytes(it.encodeToByteArray())
            }
        }

        public fun unmarshall(input: SequentialReader): TXTResourceData {
            val total = input.readShort() - 1

            var current = 0

            val txts = buildList {
                while (current < total) {
                    val len = input.read()
                    add(input.readBytes(len.toInt()).toByteArray().decodeToString())
                    current += len
                }
            }

            return TXTResourceData(txts)
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}