package dev.sitar.dns.records.data

import dev.sitar.dns.utils.NetworkAddress
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.readBytes

public data class AAAAResourceData(public val address: NetworkAddress.Ipv6Address): ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: AAAAResourceData) {
            output.writeShort(16)
            output.writeBytes(data.address.data)
        }

        public fun unmarshall(input: SequentialReader): AAAAResourceData {
            require(input.readShort() == 16.toShort())
            return AAAAResourceData(NetworkAddress.Ipv6Address(input.readBytes(16).fullSlice()))
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}