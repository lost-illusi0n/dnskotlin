package dev.sitar.dns

import dev.sitar.dns.crypto.Crypto
import dev.sitar.dns.dnssec.ValidatingDns
import dev.sitar.dns.dnssec.validating
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

public object JvmCrypto : Crypto {
    private fun ByteArray.as_rsa_key(): RSAPublicKey {
        val spec = X509EncodedKeySpec(this)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec) as RSAPublicKey
    }

    override fun rsaSha256Verify(digest: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        val key = publicKey.as_rsa_key()

        return Signature.getInstance("SHA256withRSA").apply {
            initVerify(key)
            update(digest)
        }.verify(signature)
    }

    private val ec_spec: ECParameterSpec = run {
        val p = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16)
        val a = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16)
        val b = BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16)
        val gx = BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16)
        val gy = BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
        val n = BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)
        val curve = EllipticCurve(ECFieldFp(p), a, b)

        ECParameterSpec(curve, ECPoint(gx, gy), n, 1)
    }

    private fun ByteArray.as_ec_key(): ECPublicKey {
        val kf = KeyFactory.getInstance("EC")

        val x = copyOfRange(0, 32)
        val y = copyOfRange(32, 64)

        val point = ECPoint(BigInteger(1, x), BigInteger(1, y))

        return kf.generatePublic(ECPublicKeySpec(point, ec_spec)) as ECPublicKey
    }

    override fun ecdsaSha256Verify(digest: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        val key = publicKey.as_ec_key()

        return Signature.getInstance("SHA256withECDSAinP1363Format").apply {
            initVerify(key)
            update(digest)
        }.verify(signature)
    }
}

public fun Dns.validating(): ValidatingDns = validating(JvmCrypto)