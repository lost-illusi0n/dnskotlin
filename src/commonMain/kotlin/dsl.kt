package dev.sitar.dns

import dev.sitar.dns.records.ResourceClass
import dev.sitar.dns.records.ResourceRecord
import dev.sitar.dns.records.ResourceType
import dev.sitar.dns.records.data.NSResourceData
import kotlin.random.Random

public class MessageBuilder {
    public var id: Short = Random.nextInt().toShort()
    public var qr: Boolean = false
    public var op: Op = Op.Query
    public var aa: Boolean = false
    public var rd: Boolean = true
    public var ra: Boolean = false
    public var rCode: ResponseCode = ResponseCode.NoError

    public val questions: MutableList<MessageQuestion> = mutableListOf()
    public val answers: MutableList<ResourceRecord<*>> = mutableListOf()
    public val nameServers: MutableList<ResourceRecord<NSResourceData>> = mutableListOf()
    public val additionalRecords: MutableList<ResourceRecord<*>> = mutableListOf()

    public fun query(op: Op = Op.Query, recursive: Boolean = true) {
        qr = false
        this.op = op
        this.rd = recursive
        this.rCode = ResponseCode.NoError
    }

    public fun response(
        responseCode: ResponseCode,
        op: Op = Op.Query,
        authoritative: Boolean = false,
        recursive: Boolean = false
    ) {
        qr = true
        this.op = op
        this.aa = authoritative
        this.ra = recursive
        this.rCode = responseCode
    }

    public fun question(host: String, builder: QuestionBuilder.() -> Unit = { }) {
        questions += QuestionBuilder(host).apply(builder).build()
    }

    public fun build(): Message {
        val header = MessageHeader(
            id,
            qr,
            op,
            aa,
            false,
            rd,
            ra,
            rCode,
            questions.size.toUShort(),
            answers.size.toUShort(),
            nameServers.size.toUShort(),
            additionalRecords.size.toUShort()
        )

        return Message(header, questions, answers, nameServers, additionalRecords)
    }
}

public class QuestionBuilder(public val host: String) {
    public var qType: ResourceType = ResourceType.A
    public var qClass: ResourceClass = ResourceClass.IN

    public fun build(): MessageQuestion {
        return MessageQuestion(
            host,
            qType,
            qClass
        )
    }
}

public fun message(builder: MessageBuilder.() -> Unit = { }): Message {
    return MessageBuilder().apply(builder).build()
}