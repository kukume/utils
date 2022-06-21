@file:Suppress("unused")

package me.kuku.utils

import me.kuku.pojo.CommonResult
import me.kuku.utils.MD5Utils.toMD5
import me.kuku.utils.MyUtils.regex
import java.util.function.Consumer
import java.util.HashMap
import java.lang.StringBuilder
import java.util.ArrayList

object QqUtils {
    @JvmStatic
    fun getGTK2(sKey: String): String {
        var salt: Long = 5381
        val md5key = "tencentQQVIP123443safde&!%^%1282"
        val hash: MutableList<Long> = ArrayList()
        hash.add(salt shl 5)
        val len = sKey.length
        for (i in 0 until len) {
            val ascCode = Integer.toHexString(sKey[i].code)
            val code = Integer.valueOf(ascCode, 16).toLong()
            hash.add((salt shl 5) + code)
            salt = code
        }
        val sb = StringBuilder()
        hash.forEach(Consumer { obj: Long? -> sb.append(obj) })
        var md5str = sb.toString() + md5key
        md5str = toMD5(md5str)
        return md5str
    }

    @JvmStatic
    fun getGTK(psKey: String): Long {
        val len = psKey.length
        var hash = 5381L
        for (i in 0 until len) {
            hash += (hash shl 5 and 2147483647) + psKey[i].code and 2147483647
            hash = hash and 2147483647
        }
        return hash and 2147483647
    }

    @JvmStatic
    fun getToken(token: String): Long {
        val len = token.length
        var hash = 0L
        for (i in 0 until len) {
            hash = (hash * 33 + token[i].code) % 4294967296L
        }
        return hash
    }

    @JvmStatic
    fun getToken2(token: String): Long {
        val len = token.length
        var hash = 0L
        for (i in 0 until len) {
            hash += (hash shl 5) + (token[i].code and 2147483647)
            hash = hash and 2147483647
        }
        return hash and 2147483647
    }

    @JvmStatic
    fun getResultUrl(str: String): CommonResult<String> {
        val sss = str.substring(str.indexOf("('") + 2, str.lastIndexOf('\'')).replace("', '", "")
        val arr = sss.split("','").toTypedArray()
        val ss = arr[0]
        val msg = when (ss.toInt()) {
            4 -> "验证码错误，登录失败！！"
            3 -> "密码错误，登录失败！！"
            19 -> "您的QQ号已被冻结，登录失败！"
            10009 -> return CommonResult.failure(code = 10009, message = "您的QQ号登录需要验证短信，请输入短信验证码！！", data = arr[4])
            0, 2 -> {
                val url = if (arr.size == 2) arr[1] else arr[2]
                return CommonResult.success(url)
            }
            1, -1, 7 -> "superKey已失效，请更新QQ！"
            23003 -> "当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。请尝试扫码登录。"
            10005 -> "登录环境异常（异地登录或IP存在风险）请使用QQ手机版扫码登录，保护帐号安全。"
            else -> arr[4]
        }
        return if (msg.contains("superKey"))
            CommonResult.failure(code = 502, message = msg)
        else CommonResult.failure(code = 500, message = msg)
    }

    @JvmStatic
    fun getKey(pt: String?, qq: String?, domain: String?, suffixUrl: String?): Map<String, String?> {
        return getKey(String.format("https://%s/check_sig?uin=%s&ptsigx=%s%s", domain, qq, pt, suffixUrl))
    }

    @JvmStatic
    fun getPtToken(str: String): CommonResult<String> {
        val result = getResultUrl(str)
        return if (result.code == 200) {
            val url = result.data
            val token = regex("ptsigx=", "&", url!!)
            CommonResult.success(token)
        } else result
    }

    @JvmStatic
    fun getKey(url: String?): Map<String, String?> {
        val response = OkHttpUtils.get(url!!)
        response.close()
        val cookie = OkUtils.cookie(response)
        val map: MutableMap<String, String?> = HashMap()
        map["p_skey"] = OkUtils.cookie(cookie, "p_skey")
        map["pt4_token"] = OkUtils.cookie(cookie, "pt4_token")
        map["pt_oauth_token"] = OkUtils.cookie(cookie, "pt_oauth_token")
        map["pt_login_type"] = OkUtils.cookie(cookie, "pt_login_type")
        return map
    }

    @JvmStatic
    fun encryptPassword(qq: Long, password: String, vCode: String): String {
        val map: MutableMap<String, String> = HashMap()
        map["qq"] = qq.toString()
        map["password"] = password
        map["randStr"] = vCode
        val jsonNode = OkHttpUtils.postJson("https://api.kukuqaq.com/exec/qq", map)
        return jsonNode["password"].asText()
    }
}