package com.drcorchit.justice.utils.crypto

import com.drcorchit.justice.utils.Logger
import com.drcorchit.justice.utils.crypto.CryptoUtils.ASYMMETRIC_CIPHER_TYPE
import com.drcorchit.justice.utils.crypto.CryptoUtils.CHARSET
import com.drcorchit.justice.utils.crypto.CryptoUtils.RSA_MAX_MESSAGE_LENGTH
import com.drcorchit.justice.utils.crypto.CryptoUtils.createSymmetricKey
import com.drcorchit.justice.utils.crypto.CryptoUtils.fromBase64
import com.drcorchit.justice.utils.crypto.CryptoUtils.getHash
import com.drcorchit.justice.utils.crypto.CryptoUtils.toBase64
import com.google.gson.JsonObject
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.security.*
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

class RSA(f: File) {
    val privateKey: PrivateKey
    val publicKey: PublicKey

    init {
        try {
            Security.addProvider(BouncyCastleProvider())
            if (f.exists()) {
                val obj = PemReader(FileReader(f)).readPemObject()
                //val keySpec = X509EncodedKeySpec(obj.content)
                val keySpec = PKCS8EncodedKeySpec(obj.content)
                val keyFactory = KeyFactory.getInstance(ASYMMETRIC_CIPHER_TYPE)
                privateKey = keyFactory.generatePrivate(keySpec)
                val temp = privateKey as RSAPrivateCrtKey
                val spec = RSAPublicKeySpec(temp.modulus, temp.publicExponent)
                publicKey = factory.generatePublic(spec)
                //publicKey = keyFactory.generatePublic(keySpec)
            } else {
                val generator = KeyPairGenerator.getInstance(ASYMMETRIC_CIPHER_TYPE)
                generator.initialize(2048)
                val pair: KeyPair = generator.generateKeyPair()
                privateKey = pair.private
                publicKey = pair.public

                //Write the keypair to the file
                f.parentFile.mkdirs()
                //f.createNewFile()
                val obj = PemObject("RSA PRIVATE KEY", privateKey.encoded)
                val writer = PemWriter(FileWriter(f))
                writer.writeObject(obj)
                writer.flush()
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not initialize Encryption Utils!", e)
        }
    }

    fun sign(data: String): String {
        val hash = getHash(data.toByteArray(CHARSET), RSA_MAX_MESSAGE_LENGTH)
        return toBase64(sign(hash, privateKey))
    }

    fun verify(data: String, signedHash: String): Boolean {
        return try {
            val hash = getHash(data.toByteArray(CHARSET), RSA_MAX_MESSAGE_LENGTH)
            val decryptedHash = verify(fromBase64(signedHash), publicKey)
            hash.contentEquals(decryptedHash)
        } catch (e: Exception) {
            log.error("verify", "Error while verifying signature.", e)
            false
        }
    }

    fun encrypt(message: String): Message {
        return encrypt(message.toByteArray(CHARSET))
    }

    fun encrypt(plaintext: ByteArray): Message {
        val rawKey = createSymmetricKey()
        val (first, second) = CryptoUtils.encrypt(plaintext, rawKey)
        val key: ByteArray
        val iv: ByteArray = first
        val ciphertext: ByteArray = second
        val encryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        key = encryptCipher.doFinal(rawKey.toByteArray(CHARSET))
        return Message(ciphertext, iv, key)
    }

    inner class Message constructor(val message: ByteArray, val iv: ByteArray, val key: ByteArray) {
        fun decrypt(privateKey: PrivateKey): ByteArray {
            val decryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val rawKey = String(decryptCipher.doFinal(key), CHARSET)
            return CryptoUtils.decrypt(message, iv, rawKey)
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

    fun deserialize(info: JsonObject): Message {
        return Message(
            fromBase64(info["message"].asString),
            fromBase64(info["iv"].asString),
            fromBase64(info["key"].asString)
        )
    }

    private fun sign(data: ByteArray, key: PrivateKey): ByteArray {
        val encryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
        encryptCipher.init(Cipher.ENCRYPT_MODE, key)
        return encryptCipher.doFinal(data)
    }

    private fun verify(data: ByteArray, key: PublicKey): ByteArray {
        val encryptCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_TYPE)
        encryptCipher.init(Cipher.DECRYPT_MODE, key)
        return encryptCipher.doFinal(data)
    }

    companion object {
        private val log = Logger.getLogger(RSA::class.java)

        val factory = KeyFactory.getInstance(ASYMMETRIC_CIPHER_TYPE)
    }
}