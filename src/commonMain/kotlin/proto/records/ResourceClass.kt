package dev.sitar.dns.proto.records

public sealed class ResourceClass(public val value: Short) {
    public data object IN : ResourceClass(1)
    public data object CS : ResourceClass(2)
    public data object CH : ResourceClass(3)
    public data object HS : ResourceClass(4)
    public class Other(value: Short) : ResourceClass(value) {
        override fun toString(): String = "Other(value=$value)"
    }

    public companion object {
        public val entries: Array<ResourceClass> by lazy { arrayOf(IN, CS, CH, HS) }

        public fun fromValue(value: Short): ResourceClass {
            return entries.find { it.value == value } ?: Other(value)
        }
    }
}