package com.drcorchit.utils

import com.google.gson.JsonObject
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.util.io.pem.PemObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

private val log = Logger.getLogger(Keypair::class.java)

class Keypair(path: String) {
    val privateKey: PrivateKey
    val publicKey: PublicKey

    init {
        try {
            Security.addProvider(BouncyCastleProvider())
            val f = File(path)
            if (f.exists()) {
                val parser = PEMParser(FileReader(f))
                val pemObject: PemObject = parser.readPemObject()
                val keySpec = PKCS8EncodedKeySpec(pemObject.content)
                val keyFactory = KeyFactory.getInstance(ASYMMETRIC_CIPHER_TYPE)
                privateKey = keyFactory.generatePrivate(keySpec)
                publicKey = keyFactory.generatePublic(keySpec)
            } else {
                val generator = KeyPairGenerator.getInstance(ASYMMETRIC_CIPHER_TYPE)
                generator.initialize(2048)
                val pair: KeyPair = generator.generateKeyPair()
                privateKey = pair.private
                publicKey = pair.public

                //Write the keypair to the file
                val writer = JcaPEMWriter(FileWriter(f))
                writer.writeObject(pair)
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not initialize Encryption Utils!", e)
        }
    }

    fun sign(data: String): String? {
        return try {
            val hash = getHash(data.toByteArray(CHARSET), RSA_MAX_MESSAGE_LENGTH)
            toBase64(sign(hash, privateKey))
        } catch (e: Exception) {
            log.error("sign", "Unable to sign message", e)
            null
        }
    }

    fun verify(data: String, signedHash: String?): Boolean {
        return try {
            val hash = getHash(data.toByteArray(CHARSET), RSA_MAX_MESSAGE_LENGTH)
            val decryptedHash = verify(fromBase64(signedHash), publicKey)
            hash.contentEquals(decryptedHash)
        } catch (e: Exception) {
            log.error("verify", "Error while verifying signature.", e)
            false
        }
    }

    fun encrypt(message: String): RSAMessage {
        return encrypt(message.toByteArray(CHARSET))
    }

    fun encrypt(plaintext: ByteArray): RSAMessage {
        val rawKey = createSymmetricKey()
        val (first, second) = encrypt(plaintext, rawKey)
        val ciphertext: ByteArray?
        val iv: ByteArray?
        val key: ByteArray
        iv = first
        ciphertext = second
        val encryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        key = encryptCipher.doFinal(rawKey.toByteArray(CHARSET))
        return RSAMessage(ciphertext, iv, key)
    }

    inner class RSAMessage constructor(val message: ByteArray, val iv: ByteArray, val key: ByteArray) {
        fun decrypt(privateKey: PrivateKey): ByteArray {
            val decryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val rawKey = String(decryptCipher.doFinal(key), CHARSET)
            return decrypt(message, iv, rawKey)
        }

        fun decrypt(): String {
            return String(decrypt(privateKey), CHARSET)
        }

        fun serialize(): JsonObject {
            val output = JsonObject()
            output.addProperty("message", toBase64(message))
            output.addProperty("iv", toBase64(iv))
            output.addProperty("key", toBase64(key))
            return output
        }
    }

    fun deserialize(info: JsonObject): RSAMessage {
        return RSAMessage(
            fromBase64(info["message"].asString),
            fromBase64(info["iv"].asString),
            fromBase64(info["key"].asString)
        )
    }

    @Throws(Exception::class)
    private fun sign(data: ByteArray, key: PrivateKey): ByteArray {
        val encryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
        encryptCipher.init(Cipher.ENCRYPT_MODE, key)
        return encryptCipher.doFinal(data)
    }

    @Throws(Exception::class)
    private fun verify(data: ByteArray, key: PublicKey): ByteArray {
        val encryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
        encryptCipher.init(Cipher.DECRYPT_MODE, key)
        return encryptCipher.doFinal(data)
    }
}