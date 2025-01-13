package dev.sitar.dns

import dev.sitar.dns.proto.*
import dev.sitar.dns.proto.records.ResourceClass
import dev.sitar.dns.proto.records.ResourceRecord
import dev.sitar.dns.proto.records.ResourceType
import dev.sitar.dns.proto.records.data.NSResourceData
import dev.sitar.dns.proto.records.data.OPTResourceData
import dev.sitar.dns.proto.records.data.OPTResourceRecord
import kotlin.random.Random

public class MessageBuilder {
    public var id: Short = Random.nextInt().toShort()
    public var qr: Boolean = false
    public var op: Op = Op.Query
    public var aa: Boolean = false
    public var rd: Boolean = true
    public var ra: Boolean = false
    public var z: Boolean = false
    public var ad: Boolean = false
    public var cd: Boolean = false
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

    public fun options(builder: OptionsBuilder.() -> Unit = { }) {
        additionalRecords += OptionsBuilder().apply(builder).build()
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
            z,
            ad,
            cd,
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

public class OptionsBuilder {
    public var payloadSize: Short = 4096
    public var dnssecOk: Boolean = false

    public fun build(): OPTResourceRecord {
        val flags = if (dnssecOk) (1 shl 15) else 0

        return OPTResourceRecord(
            "",
            ResourceType.OPT,
            ResourceClass.Other(payloadSize),
            flags,
            OPTResourceData(emptyList())
        )
    }
}

public fun message(builder: MessageBuilder.() -> Unit = { }): Message {
    return MessageBuilder().apply(builder).build()
}