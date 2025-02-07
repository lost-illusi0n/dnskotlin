package dev.sitar.dns.proto

import dev.sitar.dns.proto.records.ResourceClass
import dev.sitar.dns.proto.records.ResourceType
import dev.sitar.dns.proto.records.decompress_name
import dev.sitar.dns.proto.records.write_name
import kotlinx.io.Sink

public data class MessageQuestion(
    val qName: String,
    val qType: ResourceType,
    val qClass: ResourceClass
) {
    public object Factory {
        public fun marshall(output: Sink, question: MessageQuestion) {
            write_name(question.qName, output)
            output.writeShort(question.qType.value)
            output.writeShort(question.qClass.value)
        }

        public fun unmarshall(scope: MessageReadScope): MessageQuestion = scope {
            val qName = decompress_name(child())
            val qType = ResourceType.fromValue(input.readShort())
            val qClass = ResourceClass.fromValue(input.readShort())

            return MessageQuestion(qName, qType, qClass)
        }
    }
}
