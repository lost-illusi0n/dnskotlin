package dev.sitar.dns.records

import dev.sitar.dns.records.data.*
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.readBytes
import dev.sitar.kio.buffers.writeBytes
import kotlin.experimental.and
import kotlin.experimental.inv

public sealed class ResourceRecord {
    public abstract val name: String
    public abstract val type: ResourceType
    public abstract val `class`: ResourceClass
    public abstract val ttl: Int
    public abstract val data: ResourceData

    public companion object {
        public fun marshall(output: SequentialWriter, record: ResourceRecord) {
            output.writeBytes((record.name + Char(0)).encodeToByteArray())
            output.writeShort(record.type.value)
            output.writeShort(record.`class`.value)
            output.writeInt(record.ttl)

            record.data.marshall(output)
        }

        public fun unmarshall(input: SequentialReader): ResourceRecord {
            val name = decompressName(input)
            val type = ResourceType.fromValue(input.readShort())
            val `class` = ResourceClass.fromValue(input.readShort())!!
            val ttl = input.readInt()

            return when (type) {
                ResourceType.A -> AResourceRecord(name, `class`, ttl, AResourceData.unmarshall(input))
                ResourceType.NS -> NSResourceRecord(name, `class`, ttl, NSResourceData.unmarshall(input))
//                ResourceType.MD -> TODO()
//                ResourceType.MF -> TODO()
//                ResourceType.CNAME -> TODO()
//                ResourceType.SOA -> TODO()
//                ResourceType.MB -> TODO()
//                ResourceType.MG -> TODO()
//                ResourceType.MR -> TODO()
//                ResourceType.NULL -> TODO()
//                ResourceType.WKS -> TODO()
//                ResourceType.PTR -> TODO()
//                ResourceType.HINFO -> TODO()
//                ResourceType.MINFO -> TODO()
                ResourceType.MX -> MXResourceRecord(name, `class`, ttl, MXResourceData.unmarshall(input))
                ResourceType.TXT -> TXTResourceRecord(name, `class`, ttl, TXTResourceData.unmarshall(input))
                ResourceType.AAAA -> AAAAResourceRecord(name, `class`, ttl, AAAAResourceData.unmarshall(input))
                ResourceType.SRV -> SRVResourceRecord(name, `class`, ttl, SRVResourceData.unmarshall(input))
                else -> UnknownResourceRecord(name, type, `class`, ttl, UnknownResourceData.unmarshall(input))
            }
        }
    }
}

private sealed interface DomainPart {
    sealed class Label(val length: Int): DomainPart {
        object Null: Label(0)
        class Text(val text: String): Label(text.length)
    }

    class Pointer(val offset: Int): DomainPart
}

private const val LABEL_MASK = 0xc0.toByte()

private fun decompressPart(input: SequentialReader): DomainPart {
    val len = input.read()

    when (len and LABEL_MASK) {
        LABEL_MASK -> {
            val offset = (len and LABEL_MASK.inv()).toInt() shl 8 or input.read().toUByte().toInt()
            return DomainPart.Pointer(offset)
        }
        0.toByte() -> {
            if (len == 0.toByte()) return DomainPart.Label.Null

            val text = input.readBytes(len.toInt())
            return DomainPart.Label.Text(text.backingArray!!.decodeToString())
        }
        else -> error("bad flag")
    }
}

internal fun decompressName(input: SequentialReader): String {
    return buildList {
        while (true) {
            when (val part = decompressPart(input)) {
                is DomainPart.Label.Text -> {
                    add(part.text)
                }
                is DomainPart.Pointer -> {
                    val current = input.readIndex
                    input.readIndex = part.offset

                    add(decompressName(input))

                    input.readIndex = current

                    break
                }
                DomainPart.Label.Null -> break
            }
        }
    }.joinToString(".")
}