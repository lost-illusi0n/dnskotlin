package dev.sitar.dns.crypto

public interface Crypto {
    public fun rsaSha256Verify(digest: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    public fun ecdsaSha256Verify(digest: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}