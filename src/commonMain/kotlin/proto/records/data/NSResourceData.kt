package dev.sitar.dns.proto.records.data

import dev.sitar.dns.proto.MessageReadScope
import dev.sitar.dns.proto.records.decompress_name
import dev.sitar.dns.proto.records.dnsNameByteLength
import dev.sitar.dns.proto.records.write_name
import kotlinx.io.Sink

public data class NSResourceData(public val nameServer: String) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: NSResourceData) {
            output.writeShort(data.nameServer.dnsNameByteLength)
            write_name(data.nameServer, output)
        }

        public fun unmarshall(scope: MessageReadScope): NSResourceData = scope {
            input.readShort() // length
            return NSResourceData(decompress_name(child()))
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}