package dev.sitar.dns.records

import dev.sitar.dns.records.data.AResourceData

public data class AResourceRecord(
    override val name: String,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: AResourceData
): ResourceRecord() {
    override val type: ResourceType = ResourceType.A
}