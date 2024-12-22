package dev.sitar.dns.records.data

import dev.sitar.dns.records.decompressName
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.writeBytes

public data class SRVResourceData(
    val priority: UShort,
    val weight: UShort,
    val port: UShort,
    val target: String
): ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: SRVResourceData) {
            output.writeShort(data.priority.toShort())
            output.writeShort(data.weight.toShort())
            output.writeShort(data.port.toShort())
            output.writeShort(data.target.length.toShort())
            output.writeBytes(data.target.encodeToByteArray())
        }

        public fun unmarshall(input: SequentialReader): SRVResourceData {
            input.readShort()

            val priority = input.readShort().toUShort()
            val weight = input.readShort().toUShort()
            val port = input.readShort().toUShort()

            val target = decompressName(input)

            return SRVResourceData(priority, weight, port, target)
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}