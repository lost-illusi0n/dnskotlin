package dev.sitar.dns.records

import dev.sitar.dns.records.data.UnknownResourceData

public data class UnknownResourceRecord(
    override val name: String,
    override val type: ResourceType,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: UnknownResourceData
): ResourceRecord()