package dev.sitar.dns.records

public enum class ResourceType(public val value: Short) {
    A(1),
    NS(2),
    MD(3),
    MF(4),
    CNAME(5),
    SOA(6),
    MB(7),
    MG(8),
    MR(9),
    NULL(10),
    WKS(11),
    PTR(12),
    HINFO(13),
    MINFO(14),
    MX(15),
    TXT(16),
    SRV(33),
    AAAA(28),
    AXFR(252),
    MAILB(253),
    ALL(255),
    UNKNOWN(-1);

    public companion object {
        public fun fromValue(value: Short): ResourceType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}