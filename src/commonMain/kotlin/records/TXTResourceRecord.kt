package dev.sitar.dns.records

import dev.sitar.dns.records.data.TXTResourceData

public data class TXTResourceRecord(
    override val name: String,
    override val `class`: ResourceClass,
    override val ttl: Int,
    override val data: TXTResourceData
) : ResourceRecord() {
    override val type: ResourceType = ResourceType.TXT
}