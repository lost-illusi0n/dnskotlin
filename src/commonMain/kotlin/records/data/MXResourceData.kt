package dev.sitar.dns.records.data

import dev.sitar.dns.records.decompressName
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.writeBytes

public data class MXResourceData(val preference: Short, val exchange: String): ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: MXResourceData) {
            output.writeShort(data.preference)
            output.writeShort(data.exchange.length.toShort())
            output.writeBytes(data.exchange.encodeToByteArray())
        }

        public fun unmarshall(input: SequentialReader): MXResourceData {
            val preference = input.readShort()
            input.readShort() // length
            return MXResourceData(preference, decompressName(input))
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}