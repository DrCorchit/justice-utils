package com.drcorchit.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class CryptoUtilsTest {

    @Test
    fun testSymmetricEncryption() {
        val plaintext = "123 this is a test"
        val password = "abc123"
        val ciphertext: Pair<String, String> = encrypt(password, plaintext)
        //System.out.println("Encrypted: " + ciphertext);
        val decrypted: String = decrypt(password, ciphertext.first, ciphertext.second)
        //System.out.println("Decrypted: " + decrypted);
        Assertions.assertEquals(plaintext, decrypted)
    }

    @Test
    fun testSign() {
        val plaintext = "This could be a json object"
        val rsa = RSA(keypairFile)
        val signedHash: String = rsa.sign(plaintext)
        //System.out.println("Signed Hash: " + signedHash);
        Assertions.assertTrue(rsa.verify(plaintext, signedHash))
        Assertions.assertFalse(rsa.verify("changed message", signedHash))
    }

    @Test
    fun testAsymmetricEncryption() {
        val plaintext = "This is a test"
        val rsa = RSA(keypairFile)
        val message: RSA.Message = rsa.encrypt(plaintext)
        val decrypted: String = message.decrypt()
        Assertions.assertEquals(plaintext, decrypted)
    }

    companion object {
        val keypairFile = File("src/test/kotlin/com/drcorchit/utils/test.pem")

        init {
            keypairFile.deleteOnExit()
        }
    }
}