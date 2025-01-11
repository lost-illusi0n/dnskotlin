package dev.sitar.dns.dnssec

public enum class DnssecDigest(public val value: Byte) {
    Sha1(1),
    Sha256(2),
    Unknown(-1);

    public companion object {
        public fun fromValue(value: Byte): DnssecDigest {
            return entries.find { it.value == value } ?: Unknown
        }
    }
}