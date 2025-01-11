package dev.sitar.dns.dnssec

public enum class DnssecAlgorithm(public val value: Byte) {
    RsaSha1(5),
    RsaSha1Nsec3Sha1(7),
    RsaSha256(8),
    RsaSha512(10),
    EcdsaSha256(13),
    Unknown(-1);

    public companion object {
        public fun fromValue(value: Byte): DnssecAlgorithm {
            return entries.find { it.value == value } ?: Unknown
        }
    }
}