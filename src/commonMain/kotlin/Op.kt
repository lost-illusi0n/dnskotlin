package dev.sitar.dns

public enum class Op(public val value: Int) {
    Query(0),
    InverseQuery(1),
    Status(2);

    public companion object {
        public fun fromValue(value: Int): Op? {
            return values().find { it.value == value }
        }
    }
}