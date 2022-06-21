@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.kuku.utils

import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    fun encrypt(content: String, key: String, iv: String): String? {
        return try {
            val raw = key.toByteArray()
            val spec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ips = IvParameterSpec(iv.toByteArray())
            cipher.init(Cipher.ENCRYPT_MODE, spec, ips)
            val encrypted = cipher.doFinal(content.toByteArray())
            Base64.getEncoder().encodeToString(encrypted)
        } catch (e: Exception) {
            null
        }
    }

    fun decryptLoc(aseKey: ByteArray?, iv: ByteArray, data: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            val secretKey: SecretKey = SecretKeySpec(aseKey, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private const val AES_KEY_SIZE_128 = 128
    private const val AES_KEY_SIZE_192 = 192
    private const val AES_KEY_SIZE_256 = 256
    private const val RNG_ALGORITHM = "SHA1PRNG"
    private const val AES_ALGORITHM = "AES"
    private const val AES_GCM_ALGORITHM = "AES/GCM/PKCS5Padding"

    fun generateAESKey(tKeySize: Int): SecretKey? {
        var keysize = tKeySize
        try {
            // 校验密钥长度
            if (keysize != AES_KEY_SIZE_128 && keysize != AES_KEY_SIZE_192 && keysize != AES_KEY_SIZE_256) {
                keysize = AES_KEY_SIZE_128
            }
            // 创建安全随机数生成器
            val random = SecureRandom.getInstance(RNG_ALGORITHM)
            // 创建 AES 算法生成器
            val generator = KeyGenerator.getInstance(AES_ALGORITHM)
            // 初始化算法生成器
            generator.init(keysize, random)
            return generator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    fun aesEncrypt(aseKey: ByteArray?, plain: ByteArray): ByteArray? {
        try {
            val secretKey: SecretKey = SecretKeySpec(aseKey, AES_ALGORITHM)
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return cipher.doFinal(plain)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun aesEncrypt(aesKey: String, plain: String): ByteArray? {
        return aesEncrypt(aesKey.toByteArray(StandardCharsets.UTF_8), plain.toByteArray(StandardCharsets.UTF_8))
    }

    fun aesEncryptBase(aesKey: String, plain: String): String {
        val bytes = aesEncrypt(aesKey, plain)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun aesEncrypt(aseKey: ByteArray?, plain: ByteArray, nonce: ByteArray): ByteArray? {
        try {
            // Generate an AES key from the sha256 hash of the password
            val secretKeySpec: SecretKey = SecretKeySpec(aseKey, AES_ALGORITHM)
            // 获取 AES 密码器
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val zeroIv = GCMParameterSpec(128, nonce)
            // 初始化密码器（加密模型）
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, zeroIv)
            return cipher.doFinal(plain)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun aesDecrypt(aseKey: ByteArray, encrypted: ByteArray): ByteArray? {
        try {
            val secretKey: SecretKey = SecretKeySpec(aseKey, AES_ALGORITHM)
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return cipher.doFinal(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun aesDecrypt(aseKey: ByteArray, encrypted: ByteArray, nonce: ByteArray): ByteArray? {
        try {
            val secretKeySpec = SecretKeySpec(aseKey, AES_ALGORITHM)
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val zeroIv = GCMParameterSpec(128, nonce)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, zeroIv)
            return cipher.doFinal(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun generatorNonce(len: Int): ByteArray {
        val values = ByteArray(len)
        val random = SecureRandom()
        random.nextBytes(values)
        return values
    }
}