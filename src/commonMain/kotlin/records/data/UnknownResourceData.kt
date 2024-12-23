package dev.sitar.dns.records.data

import dev.sitar.dns.MessageReadScope
import io.ktor.utils.io.core.*
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

public class UnknownResourceData(public val data: ByteArray) : ResourceData() {
    public companion object {
        public fun marshall(output: Sink, data: UnknownResourceData) {
            output.writeShort(data.data.size.toShort())
            output.write(data.data)
        }

        public fun unmarshall(input: Source): UnknownResourceData {
            val length = input.readShort()
            val data = input.readByteArray(length.toInt())
            return UnknownResourceData(data)
        }
    }

    override fun marshall(output: Sink) {
        marshall(output, this)
    }
}