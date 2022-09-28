package dev.sitar.dns.records.data

import dev.sitar.kio.Slice
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.readBytes

public data class UnknownResourceData(public val data: Slice): ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: UnknownResourceData) {
            output.writeShort(data.data.length.toShort())
            output.writeBytes(data.data)
        }

        public fun unmarshall(input: SequentialReader): UnknownResourceData {
            val length = input.readShort()
            val data = input.readBytes(length.toInt())
            return UnknownResourceData(data.fullSlice())
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}