package dev.sitar.dns

import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import kotlin.experimental.and

public data class MessageHeader(
    val id: Short,
    val qr: Boolean,
    val op: Op,
    val aa: Boolean,
    val tc: Boolean,
    val rd: Boolean,
    val ra: Boolean,
    val responseCode: ResponseCode,
    val qdCount: UShort,
    val anCount: UShort,
    val nsCount: UShort,
    val arCount: UShort,
) {
    public object Factory {
        public fun marshall(output: SequentialWriter, header: MessageHeader) {
            output.writeShort(header.id)

            var b1 = header.op.value shl 3
            b1 = b1 or (header.qr.asInt shl 7)
            b1 = b1 or (header.aa.asInt shl 2)
            b1 = b1 or (header.tc.asInt shl 1)
            b1 = b1 or (header.rd.asInt shl 0)

            output.write(b1.toByte())

            var b2 = header.responseCode.value
            b2 = b2 or (header.ra.asInt shl 7)

            output.write(b2.toByte())

            output.writeShort(header.qdCount.toShort())
            output.writeShort(header.anCount.toShort())
            output.writeShort(header.nsCount.toShort())
            output.writeShort(header.arCount.toShort())
        }

        public fun unmarshall(input: SequentialReader): MessageHeader {
            val id = input.readShort()

            val b1 = input.read()
            val qr = b1.toInt() and 0x80 == 0x80
            val op = Op.fromValue((b1 and 0x78).toInt() shr 3)!!
            val aa = b1.toInt() and 0x40 == 0x40
            val tc = b1.toInt() and 0x20 == 0x20
            val rd = b1.toInt() and 0x10 == 0x10

            val b2 = input.read()
            val ra = b2.toInt() and 0x80 == 0x80
            val responseCode = ResponseCode.fromValue(b2.toInt() and 0xF)!!

            val qdCount = input.readShort().toUShort()
            val anCount = input.readShort().toUShort()
            val nsCount = input.readShort().toUShort()
            val arCount = input.readShort().toUShort()

            return MessageHeader(id, qr, op, aa, tc, rd, ra, responseCode, qdCount, anCount, nsCount, arCount)
        }
    }
}

private val Boolean.asInt: Int
    get() = when (this) {
        true -> 1
        false -> 0
    }