package dev.sitar.dns

import dev.sitar.dns.records.NSResourceRecord
import dev.sitar.dns.records.ResourceRecord
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter

public data class Message(
    val header: MessageHeader,
    val questions: List<MessageQuestion>,
    val answers: List<ResourceRecord>,
    val nameServers: List<NSResourceRecord>,
    val additionalRecords: List<ResourceRecord>
) {
    public object Factory {
        public fun marshall(output: SequentialWriter, message: Message) {
            MessageHeader.Factory.marshall(output, message.header)
            message.questions.forEach { MessageQuestion.Factory.marshall(output, it) }
            message.answers.forEach { ResourceRecord.marshall(output, it) }
            message.nameServers.forEach { ResourceRecord.marshall(output, it) }
            message.additionalRecords.forEach { ResourceRecord.marshall(output, it) }
        }

        public fun unmarshall(input: SequentialReader): Message {
            val header = MessageHeader.Factory.unmarshall(input)

            val questions = buildList {
                repeat(header.qdCount.toInt()) {
                    add(MessageQuestion.Factory.unmarshall(input))
                }
            }

            val answers = buildList {
                repeat(header.anCount.toInt()) {
                    add(ResourceRecord.unmarshall(input))
                }
            }

            val nameServers = buildList {
                repeat(header.nsCount.toInt()) {
                    add(ResourceRecord.unmarshall(input) as NSResourceRecord)
                }
            }

            val additionalRecords = buildList {
                repeat(header.arCount.toInt()) {
                    add(ResourceRecord.unmarshall(input))
                }
            }

            return Message(header, questions, answers, nameServers, additionalRecords)
        }
    }
}
