package dev.sitar.dns.records.data

import dev.sitar.dns.records.decompressName
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.writeBytes

public data class NSResourceData(public val nameServer: String) : ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: NSResourceData) {
            output.writeShort(data.nameServer.length.toShort())
            output.writeBytes(data.nameServer.encodeToByteArray())
        }

        public fun unmarshall(input: SequentialReader): NSResourceData {
            input.readShort() // length
            return NSResourceData(decompressName(input))
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}