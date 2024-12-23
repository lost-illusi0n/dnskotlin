package dev.sitar.dns.records

import dev.sitar.dns.MessageReadScope
import dev.sitar.dns.records.data.*
import kotlinx.io.*
import kotlin.experimental.and
import kotlin.experimental.inv

public data class ResourceRecord<T : ResourceData>(
    public val name: String,
    public val type: ResourceType,
    public val `class`: ResourceClass,
    public val ttl: Int,
    public val data: T
) {
    public companion object {
        public fun marshall(output: Sink, record: ResourceRecord<*>) {
            output.writeString(record.name + Char(0))
            output.writeShort(record.type.value)
            output.writeShort(record.`class`.value)
            output.writeInt(record.ttl)

            record.data.marshall(output)
        }


        public fun unmarshall(scope: MessageReadScope): ResourceRecord<*> = scope {
            val name = decompressName(child())
            val type = ResourceType.fromValue(input.readShort())
            val `class` = ResourceClass.fromValue(input.readShort())!!
            val ttl = input.readInt()

            val data = when (type) {
                ResourceType.A -> AResourceData.unmarshall(input)
                ResourceType.NS -> NSResourceData.unmarshall(child())
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
                ResourceType.MX -> MXResourceData.unmarshall(child())
                ResourceType.TXT -> TXTResourceData.unmarshall(input)
                ResourceType.AAAA -> AAAAResourceData.unmarshall(input)
                ResourceType.SRV -> SRVResourceData.unmarshall(child())
                else -> UnknownResourceData.unmarshall(input)
            }

            return ResourceRecord(name, type, `class`, ttl, data)
        }
    }
}

private sealed interface DomainPart {
    sealed class Label(val length: Int) : DomainPart {
        data object Null : Label(0)
        class Text(val text: String) : Label(text.length)
    }

    class Pointer(val offset: Int) : DomainPart
}

private const val LABEL_MASK = 0xc0.toByte()

private fun decompressPart(input: Source): DomainPart {
    val len = input.readByte()

    when (len and LABEL_MASK) {
        LABEL_MASK -> {
            val offset = (len and LABEL_MASK.inv()).toInt() shl 8 or input.readByte().toUByte().toInt()
            return DomainPart.Pointer(offset)
        }

        0.toByte() -> {
            if (len == 0.toByte()) return DomainPart.Label.Null

            return DomainPart.Label.Text(input.readString(len.toLong()))
        }

        else -> error("bad flag")
    }
}

internal fun decompressName(scope: MessageReadScope): String = scope {
    return buildList {
        while (true) {
            when (val part = decompressPart(input)) {
                is DomainPart.Label.Text -> {
                    add(part.text)
                }

                is DomainPart.Pointer -> {
                    val pointerSource = readSource.peek()

                    pointerSource.skip(part.offset.toLong())

                    add(decompressName(child(current = pointerSource)))

                    break
                }

                DomainPart.Label.Null -> break
            }
        }
    }.joinToString(".")
}