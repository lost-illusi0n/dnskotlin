package dev.sitar.dns.proto

public enum class Op(public val value: Int) {
    Query(0),
    InverseQuery(1),
    Status(2);

    public companion object {
        public fun fromValue(value: Int): Op? {
            return entries.find { it.value == value }
        }
    }
}