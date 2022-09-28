package dev.sitar.dns

public enum class ResponseCode(public val value: Int) {
    NoError(0),
    FormatError(1),
    ServerFailure(2),
    NameError(3),
    NotImplemented(4),
    Refused(5);

    public companion object {
        public fun fromValue(value: Int): ResponseCode? {
            return values().find { it.value == value }
        }
    }
}