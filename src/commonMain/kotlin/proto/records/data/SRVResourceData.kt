package dev.sitar.dns.proto.records.data

import dev.sitar.dns.proto.MessageReadScope
import dev.sitar.dns.proto.records.decompress_name
import kotlinx.io.Sink

public data class SRVResourceData(
    val priority: UShort,
    val weight: UShort,
    val port: UShort,
    val target: String
) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: SRVResourceData) {
            output.writeShort(data.priority.toShort())
            output.writeShort(data.weight.toShort())
            output.writeShort(data.port.toShort())
            output.writeShort(data.target.length.toShort())
            output.write(data.target.encodeToByteArray())
        }

        public fun unmarshall(scope: MessageReadScope): SRVResourceData = scope {
            input.readShort()

            val priority = input.readShort().toUShort()
            val weight = input.readShort().toUShort()
            val port = input.readShort().toUShort()

            val target = decompress_name(child())

            return SRVResourceData(priority, weight, port, target)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}