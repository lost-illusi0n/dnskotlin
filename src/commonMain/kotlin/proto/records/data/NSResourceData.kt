package dev.sitar.dns.proto.records.data

import dev.sitar.dns.proto.MessageReadScope
import dev.sitar.dns.proto.records.decompressName
import dev.sitar.dns.proto.records.dnsNameByteLength
import dev.sitar.dns.proto.records.writeName
import kotlinx.io.Sink

public data class NSResourceData(public val nameServer: String) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: NSResourceData) {
            output.writeShort(data.nameServer.dnsNameByteLength)
            writeName(data.nameServer, output)
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