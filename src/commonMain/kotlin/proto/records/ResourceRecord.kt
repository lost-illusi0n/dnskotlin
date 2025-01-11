package dev.sitar.dns.proto.records

import dev.sitar.dns.proto.MessageReadScope
import dev.sitar.dns.proto.records.data.*
import kiso.common.logger
import kiso.log.trace
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readString
import kotlin.experimental.and
import kotlin.experimental.inv

private val LOG = logger("parser(resource_record)")

public data class ResourceRecord<T : ResourceData>(
    public val name: String,
    public val type: ResourceType,
    public val `class`: ResourceClass,
    public val ttl: Int,
    public val data: T
) {
    public companion object {
        public fun marshall(output: Sink, record: ResourceRecord<*>) {
            write_name(record.name, output)
            output.writeShort(record.type.value)
            output.writeShort(record.`class`.value)
            output.writeInt(record.ttl)

            record.data.marshall(output)
        }


        public fun unmarshall(scope: MessageReadScope): ResourceRecord<*> = scope {
            val name = decompress_name(child())
            val type = ResourceType.fromValue(input.readShort())
            val `class` = ResourceClass.fromValue(input.readShort())
            val ttl = input.readInt()

            LOG.trace("parsed record header $name $type $`class` $ttl")

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
                ResourceType.OPT -> OPTResourceData.unmarshall(input)
                ResourceType.DS -> DSResourceData.unmarshall(input)
                ResourceType.RRSIG -> RRSIGResourceData.unmarshall(child())
                ResourceType.DNSKEY -> DNSKEYResourceData.unmarshall(input)
//                ResourceType.AXFR -> TODO()
//                ResourceType.MAILB -> TODO()
//                ResourceType.ALL -> TODO()
                else -> UnknownResourceData.unmarshall(input)
            }

            return ResourceRecord(name, type, `class`, ttl, data)
        }
    }
}

// convoluted but the byte length of the name consists of label lengths and a null byte.
// in the case of ROOT (empty), it is simply the terminating null byte. otherwise...
// the middle label lengths are replaced as dots in the string representation
// the first label length is not kept in the string, so that is counted as another byte
// the null byte at the end is not included, so that is counted as another byte
internal val String.dnsNameByteLength: Short get() = if (isEmpty()) 1 else (length + 2).toShort()

// doesnt compress.
internal fun write_name(name: String, sink: Sink) {
    if (name.isNotEmpty()) {
        val parts = name.split('.')

        parts.forEach {
            sink.writeByte(it.length.toByte())
            sink.write(it.encodeToByteArray())
        }
    }

    sink.writeByte(0)
}

private sealed interface DomainPart {
    sealed class Label(val length: Int) : DomainPart {
        data object Null : Label(0)
        class Text(val text: String) : Label(text.length)
    }

    class Pointer(val offset: Int) : DomainPart
}

private const val LABEL_MASK = 0xc0.toByte()

private fun decompress_part(input: Source): DomainPart {
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

        else -> {
            error("bad flag")
        }
    }
}

internal fun decompress_name(scope: MessageReadScope): String = scope {
    return buildList {
        while (true) {
            when (val part = decompress_part(input)) {
                is DomainPart.Label.Text -> {
                    add(part.text)
                }

                is DomainPart.Pointer -> {
                    val pointerSource = readSource.peek()

                    pointerSource.skip(part.offset.toLong())

                    add(decompress_name(child(current = pointerSource)))

                    break
                }

                DomainPart.Label.Null -> break
            }
        }
    }.joinToString(".")
}