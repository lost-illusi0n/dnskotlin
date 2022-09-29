package dev.sitar.dns

import dev.sitar.dns.records.ResourceClass
import dev.sitar.dns.records.ResourceType
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.readBytes
import dev.sitar.kio.buffers.writeBytes

public data class MessageQuestion(
    val qName: String,
    val qType: ResourceType,
    val qClass: ResourceClass
) {
    public object Factory {
        public fun marshall(output: SequentialWriter, question: MessageQuestion) {
            question.qName.split('.').forEach {
                output.write(it.length.toByte())
                output.writeBytes(it.encodeToByteArray())
            }

            output.write(0)

            output.writeShort(question.qType.value)
            output.writeShort(question.qClass.value)
        }

        // TODO: we can use buffers probably to optimize domain name reading
        public fun unmarshall(input: SequentialReader): MessageQuestion {
            var length = input.read().toInt()

            val qName = buildString {
                while (length != 0) {
                    append(input.readBytes(length).toByteArray().decodeToString())
                    length = input.read().toInt()
                    if (length != 0) append('.')
                }
            }

            val qType = ResourceType.fromValue(input.readShort())
            val qClass = ResourceClass.fromValue(input.readShort())!!

            return MessageQuestion(qName, qType, qClass)
        }
    }
}
