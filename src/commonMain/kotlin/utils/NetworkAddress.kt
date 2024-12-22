package dev.sitar.dns.utils

import dev.sitar.kio.Slice
import dev.sitar.kio.buffers.ByteArrayBuffer

public sealed class NetworkAddress(public val data: Slice) {
    public class Ipv4Address(data: Slice) : NetworkAddress(data) {
        init {
            require(data.length == 4)
        }

        override fun toString(): String {
            return "${data[0].toUByte()}.${data[1].toUByte()}.${data[2].toUByte()}.${data[3].toUByte()}"
        }
    }

    public class Ipv6Address(data: Slice) : NetworkAddress(data) {
        init {
            require(data.length == 16)
        }

        override fun toString(): String {
            return buildString {
                val buffer = ByteArrayBuffer(data.bytes)
                buffer.readIndex = data.start

                repeat(8) {
                    val segment = buffer.readShort()
                    append(segment.toUShort().toString(16))
                    append(":")
                }
            }
        }
    }
}