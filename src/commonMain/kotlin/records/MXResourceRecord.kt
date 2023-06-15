package dev.sitar.dns.records

import dev.sitar.dns.records.data.MXResourceData

public data class MXResourceRecord(
    override val name: String,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: MXResourceData
): ResourceRecord() {
    override val type: ResourceType = ResourceType.MX
}