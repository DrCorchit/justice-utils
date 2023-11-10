package com.drcorchit.justice.utils

import com.drcorchit.justice.utils.Logger.Companion.getLogger
import com.drcorchit.justice.utils.math.MathUtils
import java.nio.charset.StandardCharsets
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoUtils {}

private val log = getLogger(CryptoUtils::class.java)

const val RSA_MAX_MESSAGE_LENGTH = 245
val CHARSET = StandardCharsets.UTF_8
const val ASYMMETRIC_CIPHER_TYPE = "RSA"
const val SYMMETRIC_CIPHER_TYPE = "AES/CBC/PKCS5Padding"

fun createSymmetricKey(): String {
    val generator = KeyGenerator.getInstance("AES")
    generator.init(256) // The AES key size in number of bits
    return toBase64(generator.generateKey().encoded)
}

private fun passwordToAESKey(password: String): SecretKeySpec {
    val bytes = password.toByteArray(CHARSET)
    val output: ByteArray = if (bytes.size <= 16) {
        ByteArray(16)
    } else if (bytes.size <= 24) {
        ByteArray(24)
    } else {
        ByteArray(32)
    }
    val copyLen = MathUtils.min(bytes.size, output.size)
    System.arraycopy(bytes, 0, output, 0, copyLen)
    return SecretKeySpec(output, SYMMETRIC_CIPHER_TYPE)
}

fun getHash(input: String): String {
    val bytes = input.toByteArray()
    return toBase64(getHash(bytes, bytes.size))
}

fun getHash(input: ByteArray, length: Int): ByteArray {
    val hash = ByteArray(length)
    val d = MessageDigest.getInstance("SHA-256")
    d.update(input)
    d.digest(hash, 0, hash.size)
    return hash
}

fun encrypt(plaintext: ByteArray, password: String): Pair<ByteArray, ByteArray> {
    val cipher = Cipher.getInstance(SYMMETRIC_CIPHER_TYPE)
    cipher.init(Cipher.ENCRYPT_MODE, passwordToAESKey(password))
    val ciphertext = cipher.doFinal(plaintext)
    return Pair(cipher.iv, ciphertext)
}

fun decrypt(ciphertext: ByteArray, iv: ByteArray, password: String): ByteArray {
    val cipher = Cipher.getInstance(SYMMETRIC_CIPHER_TYPE)
    cipher.init(Cipher.DECRYPT_MODE, passwordToAESKey(password), IvParameterSpec(iv))
    return cipher.doFinal(ciphertext)
}

fun encrypt(password: String, plaintext: String): Pair<String, String> {
    val pair = encrypt(plaintext.toByteArray(CHARSET), password)
    return Pair(toBase64(pair.first), toBase64(pair.second))
}

fun decrypt(password: String, iv: String, ciphertext: String): String {
    val plaintext = decrypt(fromBase64(ciphertext), fromBase64(iv), password)
    return String(plaintext, CHARSET)
}

fun toBase64(data: ByteArray?): String {
    return Base64.getEncoder().encodeToString(data)
}

fun fromBase64(data: String?): ByteArray {
    return Base64.getDecoder().decode(data)
}