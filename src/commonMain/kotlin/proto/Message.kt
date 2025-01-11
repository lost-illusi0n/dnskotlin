package dev.sitar.dns.proto

import dev.sitar.dns.proto.records.ResourceRecord
import kiso.common.logger
import kiso.log.trace
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source

public data class MessageReadScope(val input: Source, val readSource: Source) {
    public fun child(current: Source = input): MessageReadScope = MessageReadScope(current, readSource.peek())

    public inline operator fun <T> invoke(block: MessageReadScope.() -> T): T {
        return block(this)
    }
}

private val LOG = logger("parser(message)")

public data class Message(
    val header: MessageHeader,
    val questions: List<MessageQuestion>,
    val answers: List<ResourceRecord<*>>,
    val authoritativeRecords: List<ResourceRecord<*>>,
    val additionalRecords: List<ResourceRecord<*>>
) {
    public object Factory {
        public fun marshall(output: Sink, message: Message) {
            MessageHeader.Factory.marshall(output, message.header)
            message.questions.forEach { MessageQuestion.Factory.marshall(output, it) }
            message.answers.forEach { ResourceRecord.marshall(output, it) }
            message.authoritativeRecords.forEach { ResourceRecord.marshall(output, it) }
            message.additionalRecords.forEach { ResourceRecord.marshall(output, it) }
        }

        public fun unmarshall(input: Source): Message {
            val base = Buffer()
            base.transferFrom(input)

            val current = base.peek()

            val header = MessageHeader.Factory.unmarshall(current)

            LOG.trace("parsed header $header")

            val scope = MessageReadScope(current, base.peek())

            val questions = buildList {
                repeat(header.qdCount.toInt()) {
                    add(MessageQuestion.Factory.unmarshall(scope.child()))
                }
            }

            val answers = buildList {
                repeat(header.anCount.toInt()) {
                    add(ResourceRecord.unmarshall(scope.child()))
                }
            }

            val authoritativeRecords = buildList {
                repeat(header.nsCount.toInt()) {
                    add(ResourceRecord.unmarshall(scope.child()))
                }
            }

            val additionalRecords = buildList {
                repeat(header.arCount.toInt()) {
                    add(ResourceRecord.unmarshall(scope.child()))
                }
            }

            return Message(header, questions, answers, authoritativeRecords, additionalRecords)
        }
    }
}
