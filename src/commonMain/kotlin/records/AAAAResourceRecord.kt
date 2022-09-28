package dev.sitar.dns.records

import dev.sitar.dns.records.data.AAAAResourceData

public data class AAAAResourceRecord(
    override val name: String,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: AAAAResourceData
) : ResourceRecord() {
    override val type: ResourceType = ResourceType.AAAA
}