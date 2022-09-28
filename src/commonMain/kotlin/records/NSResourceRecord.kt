package dev.sitar.dns.records

import dev.sitar.dns.records.data.NSResourceData

public data class NSResourceRecord(
    override val name: String,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: NSResourceData
) : ResourceRecord() {
    override val type: ResourceType = ResourceType.NS
}