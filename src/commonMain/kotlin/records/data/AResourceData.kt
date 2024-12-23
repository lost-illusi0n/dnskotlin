package dev.sitar.dns.records.data

import dev.sitar.dns.MessageReadScope
import dev.sitar.dns.utils.NetworkAddress
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

public data class AResourceData(public val address: NetworkAddress.Ipv4Address) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: AResourceData) {
            output.writeShort(4)
            output.write(data.address.data)
        }

        public fun unmarshall(input: Source): AResourceData {
            require(input.readShort() == 4.toShort())
            return AResourceData(NetworkAddress.Ipv4Address(input.readByteArray(4)))
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}