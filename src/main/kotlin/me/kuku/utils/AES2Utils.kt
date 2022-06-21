@file:Suppress("unused")

package me.kuku.utils

import okhttp3.internal.and
import java.lang.Exception
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES2Utils {

    private val IV_PARAMETER_SPEC = IvParameterSpec("0000000000000000".toByteArray())

    fun encryptIntoHexString(data: String, key: String): String? {
        try {
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IV_PARAMETER_SPEC)
            return bytesConvertHexString(
                cipher.doFinal(
                    data.toByteArray().copyOf(16 * (data.toByteArray().size / 16 + 1))
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decryptByHexString(data: String, key: String): String? {
        try {
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IV_PARAMETER_SPEC)
            return String(cipher.doFinal(hexStringConvertBytes(data.lowercase(Locale.getDefault()))), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decryptByHexString(data: String, key: String, iv: ByteArray): String? {
        try {
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv))
            return String(cipher.doFinal(hexStringConvertBytes(data.lowercase(Locale.getDefault()))), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun bytesConvertHexString(data: ByteArray): String {
        val result = StringBuffer()
        var hexString: String
        for (b in data) {
            // 补码成正十进制后转换成16进制
            hexString = Integer.toHexString(b and 255)
            result.append(if (hexString.length == 1) "0$hexString" else hexString)
        }
        return result.toString().uppercase(Locale.getDefault())
    }

    private fun hexStringConvertBytes(data: String): ByteArray {
        val length = data.length / 2
        val result = ByteArray(length)
        for (i in 0 until length) {
            val first = data.substring(i * 2, i * 2 + 1).toInt(16)
            val second = data.substring(i * 2 + 1, i * 2 + 2).toInt(16)
            result[i] = (first * 16 + second).toByte()
        }
        return result
    }
}