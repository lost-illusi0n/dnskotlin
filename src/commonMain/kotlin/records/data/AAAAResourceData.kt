package dev.sitar.dns.records.data

import dev.sitar.dns.MessageReadScope
import dev.sitar.dns.utils.NetworkAddress
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

public data class AAAAResourceData(public val address: NetworkAddress.Ipv6Address) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: AAAAResourceData) {
            output.writeShort(16)
            output.write(data.address.data)
        }

        public fun unmarshall(input: Source): AAAAResourceData {
            require(input.readShort() == 16.toShort())
            return AAAAResourceData(NetworkAddress.Ipv6Address(input.readByteArray(16)))
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}