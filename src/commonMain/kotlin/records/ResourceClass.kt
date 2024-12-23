package dev.sitar.dns.records

public enum class ResourceClass(public val value: Short) {
    IN(1),
    CS(2),
    CH(3),
    HS(4);

    public companion object {
        public fun fromValue(value: Short): ResourceClass? {
            return entries.find { it.value == value }
        }
    }
}