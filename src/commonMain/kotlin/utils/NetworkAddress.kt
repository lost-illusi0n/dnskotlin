package dev.sitar.dns.utils

public sealed class NetworkAddress(public val data: ByteArray) {
    public class Ipv4Address(data: ByteArray) : NetworkAddress(data) {
        init {
            require(data.size == 4)
        }

        override fun toString(): String {
            return "${data[0].toUByte()}.${data[1].toUByte()}.${data[2].toUByte()}.${data[3].toUByte()}"
        }
    }

    public class Ipv6Address(data: ByteArray) : NetworkAddress(data) {
        init {
            require(data.size == 16)
        }

        override fun toString(): String {
            return buildString {
                repeat(8) {
                    val offset = it * 2
                    val segment = (data[offset].toInt() and 0xFF shl 8) or (data[offset + 1].toInt() and 0xFF)

                    append(segment.toUShort().toString(16))
                    append(":")
                }
            }
        }
    }
}