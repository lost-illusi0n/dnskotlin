package dev.sitar.dns.records.data

import dev.sitar.dns.MessageReadScope
import dev.sitar.dns.records.decompressName
import kotlinx.io.Sink

public data class NSResourceData(public val nameServer: String) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: NSResourceData) {
            output.writeShort(data.nameServer.length.toShort())
            output.write(data.nameServer.encodeToByteArray())
        }

        public fun unmarshall(scope: MessageReadScope): NSResourceData = scope {
            input.readShort() // length
            return NSResourceData(decompressName(child()))
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}