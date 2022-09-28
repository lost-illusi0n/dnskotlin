package dev.sitar.dns.records.data

import dev.sitar.dns.utils.NetworkAddress
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.readBytes

public data class AResourceData(public val address: NetworkAddress.Ipv4Address): ResourceData() {
    public companion object {
        public fun marshall(output: SequentialWriter, data: AResourceData) {
            output.writeShort(4)
            output.writeBytes(data.address.data)
        }

        public fun unmarshall(input: SequentialReader): AResourceData {
            require(input.readShort() == 4.toShort())
            return AResourceData(NetworkAddress.Ipv4Address(input.readBytes(4).fullSlice()))
        }
    }

    override fun marshall(output: SequentialWriter) {
        marshall(output, this)
    }
}