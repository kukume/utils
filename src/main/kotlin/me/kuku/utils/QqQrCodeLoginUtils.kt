@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.kuku.utils

import me.kuku.pojo.CommonResult
import me.kuku.pojo.QqLoginPojo
import me.kuku.pojo.UA
import java.util.*

object QqQrCodeLoginUtils {

    suspend fun getQrCode(appId: Long = 549000912L, daId: Int = 5, ptAid: Long = 0): QqLoginQrcode {
        val response = OkHttpKtUtils.get(
            "https://ssl.ptlogin2.qq.com/ptqrshow?appid=$appId&e=2&l=M&s=3&d=72&v=4&t=0.${MyUtils.randomNum(17)}&daid=$daId&pt_3rd_aid=0$ptAid",
            OkUtils.referer("https://xui.ptlogin2.qq.com/")
        )

        val bytes = OkUtils.bytes(response)
        val cookie = OkUtils.cookie(response)
        val sig = OkUtils.cookie(cookie, "qrsig")!!
        return QqLoginQrcode(Base64.getEncoder().encodeToString(bytes), sig)
    }

    suspend fun getQrcode(qqApp: QqApp): QqLoginQrcode {
        return getQrCode(qqApp.appId, qqApp.daId, qqApp.ptAid)
    }

    suspend fun checkQrcode(qqApp: QqApp, url: String, sig: String): CommonResult<QqLoginPojo> {
        return checkQrCode(qqApp.appId, qqApp.daId, qqApp.ptAid, url, sig)
    }

    suspend fun checkQrCode(appId: Long = 549000912L, daId: Int = 5, ptAid: Long = 0, url: String = "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone", sig: String): CommonResult<QqLoginPojo> {
        val response = OkHttpKtUtils.get(
            "https://ssl.ptlogin2.qq.com/ptqrlogin?u1=${url.toUrlEncode()}&ptqrtoken=${getPtGrToken(sig)}&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=0-0-1591074900575&js_ver=20032614&js_type=1&login_sig=&pt_uistyle=40&aid=$appId&daid=$daId&pt_3rd_aid=$ptAid&",
            OkUtils.cookie("qrsig=$sig")
        )
        val str = OkUtils.str(response)
        return when (MyUtils.regex("'", "','", str)!!.toInt()) {
            0 -> {
                val cookie = OkUtils.cookie(response)
                val cookieMap = OkUtils.cookie(cookie, "skey", "superkey", "supertoken")
                val qqq = OkUtils.cookie(cookie, "pt2gguin")
                val qqStr = MyUtils.regex("[1-9][0-9]{4,}", qqq!!)
                val qq = qqStr?.toLong() ?: 0L
                val result = QqUtils.getResultUrl(str)
                val map = QqUtils.getKey(result.data)
                val qqLoginPojo = QqLoginPojo(
                    qq, cookieMap["skey"]!!, map["p_skey"]!!,
                    cookieMap["superkey"]!!, cookieMap["supertoken"]!!, map["pt4_token"]!!,
                    map["pt_oauth_token"]!!, map["pt_login_type"]!!
                )
                CommonResult.success(qqLoginPojo)
            }
            66, 67 -> CommonResult.failure(code = 0, message = "未失效或者验证中！")
            else -> CommonResult.failure(MyUtils.regex("','','0','", "', ''", str) ?: "", null)
        }
    }

    suspend fun authorize(qqApp: QqApp, sig: String, state: String, redirectUri: String): CommonResult<String> {
        val loginResult = checkQrCode(
            qqApp.appId,
            qqApp.daId,
            qqApp.ptAid,
            "https://graph.qq.com/oauth2.0/login_jump",
            sig
        )
        return if (loginResult.success()) {
            val qqLoginPojo = loginResult.data()
            authorize(qqLoginPojo, qqApp.ptAid, state, redirectUri)
        } else CommonResult.failure(code = loginResult.code, message = loginResult.message)
    }

    suspend fun authorize(qqLoginPojo: QqLoginPojo, clientId: Long, state: String, redirectUri: String): CommonResult<String> {
        val map = mutableMapOf<String, String>()
        map["response_type"] = "code"
        map["client_id"] = clientId.toString()
        map["redirect_uri"] = redirectUri
        map["scope"] = "all"
        map["state"] = state
        map["switch"] = ""
        map["from_ptlogin"] = "1"
        map["src"] = "1"
        map["update_auth"] = "1"
        map["openapi"] = "80901010"
        map["g_tk"] = qqLoginPojo.gtkP
        map["auth_time"] = System.currentTimeMillis().toString()
        val ui = UUID.randomUUID().toString()
        map["ui"] = ui
        val response = OkHttpKtUtils.post(
            "https://graph.qq.com/oauth2.0/authorize",
            map, OkUtils.headers(
                "${qqLoginPojo.authorizeCookie}ui=$ui; ",
                "https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=$clientId&response_type=code&scope=all&redirect_uri=${redirectUri.toUrlEncode()}",
                UA.PC
            )
        ).also { it.close() }
        val url = response.header("location")
        return if (url!!.contains("https://graph.qq.com/oauth2.0/show?which=error"))
            CommonResult.failure("失败，请重试！")
        else CommonResult.success(url)
    }

    private fun getPtGrToken(sig: String): Int {
        var e = 0
        var i = 0
        val n = sig.length
        while (n > i) {
            e += (e shl 5) + sig[i].code
            ++i
        }
        return e and 2147483647
    }
}

data class QqApp(
    var appId: Long = 0,
    var daId: Int = 0,
    var ptAid: Long = 0
)

data class QqLoginQrcode (
    var imageBase: String = "",
    var sig: String = ""
)