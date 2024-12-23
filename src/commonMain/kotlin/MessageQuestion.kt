package dev.sitar.dns

import dev.sitar.dns.records.ResourceClass
import dev.sitar.dns.records.ResourceType
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readString

public data class MessageQuestion(
    val qName: String,
    val qType: ResourceType,
    val qClass: ResourceClass
) {
    public object Factory {
        public fun marshall(output: Sink, question: MessageQuestion) {
            question.qName.split('.').forEach {
                output.writeByte(it.length.toByte())
                output.write(it.encodeToByteArray())
            }

            output.writeByte(0)

            output.writeShort(question.qType.value)
            output.writeShort(question.qClass.value)
        }

        public fun unmarshall(input: Source): MessageQuestion {
            var length = input.readByte().toInt()

            val qName = buildString {
                while (length != 0) {
                    append(input.readString(length.toLong()))
                    length = input.readByte().toInt()
                    if (length != 0) append('.')
                }
            }

            val qType = ResourceType.fromValue(input.readShort())
            val qClass = ResourceClass.fromValue(input.readShort())!!

            return MessageQuestion(qName, qType, qClass)
        }
    }
}
