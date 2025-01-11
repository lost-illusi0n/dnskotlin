package dev.sitar.dns.proto.records.data

import dev.sitar.dns.proto.MessageReadScope
import dev.sitar.dns.proto.records.decompress_name
import kotlinx.io.Sink

public data class MXResourceData(val preference: Short, val exchange: String) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: MXResourceData) {
            output.writeShort(data.preference)
            output.writeShort(data.exchange.length.toShort())
            output.write(data.exchange.encodeToByteArray())
        }

        public fun unmarshall(scope: MessageReadScope): MXResourceData = scope {
            val preference = input.readShort()
            input.readShort() // length
            return MXResourceData(preference, decompress_name(scope))
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}