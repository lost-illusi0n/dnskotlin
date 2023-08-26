package dev.sitar.dns.records

import dev.sitar.dns.records.data.SRVResourceData

public data class SRVResourceRecord(
    override val name: String,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: SRVResourceData
) : ResourceRecord() {
    override val type: ResourceType = ResourceType.SRV
}