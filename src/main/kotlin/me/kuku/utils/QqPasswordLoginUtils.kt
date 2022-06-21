@file:Suppress("unused", "DuplicatedCode")

package me.kuku.utils

import me.kuku.pojo.CommonResult
import me.kuku.pojo.QqLoginNeedSmsException
import me.kuku.pojo.QqLoginPojo
import me.kuku.pojo.UA

object QqPasswordLoginUtils {

    data class QqVc(
        val appId: Long,
        val daId: Int,
        var cookie: String,
        val loginSig: String,
        val ptdRvs: String,
        val redirectUrl: String,
        val enRedirectUrl: String,
        val xuiUrl: String,

        var needCaptcha: Boolean = false,
        var code: Int = 0,
        var randStr: String = "",
        var ticket: String = "",
        var sid: String = "",

        var smsCode: String = ""
    )

    private suspend fun checkVc(qqApp: QqApp, qq: Long, redirectUrl: String): QqVc {
        val enRedirectUrl = redirectUrl.toUrlEncode()
        val xuiUrl = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=${qqApp.appId}&style=20&s_url=$enRedirectUrl&maskOpacity=60&daid=${qqApp.daId}&target=self"
        val xuiResponse = OkHttpKtUtils.get(xuiUrl, OkUtils.headers("", "https://xui.ptlogin2.qq.com/", UA.PC)).also { it.close() }
        val xuiCookie = OkUtils.cookie(xuiResponse) + "ptui_loginuin=$qq; "
        val loginSig = OkUtils.cookie(xuiCookie, "pt_login_sig")!!
        val checkResponse = OkHttpKtUtils.get("https://ssl.ptlogin2.qq.com/check?regmaster=&pt_tea=2&pt_vcode=1&uin=$qq&appid=${qqApp.appId}&js_ver=21110815&js_type=1&login_sig=$loginSig&u1=$enRedirectUrl&r=0.${MyUtils.randomNum(15)}&pt_uistyle=40",
            OkUtils.headers(xuiCookie, "https://xui.ptlogin2.qq.com/", UA.PC))
        val checkCookie = OkUtils.cookie(checkResponse)
        val cookie = xuiCookie + checkCookie
        val ptdRvs = OkUtils.cookie(checkCookie, "ptdrvs")
        val ptVfSession = OkUtils.cookie(checkCookie, "ptvfsession")
        val checkStr = OkUtils.str(checkResponse)
        val arr = checkStr.substring(checkStr.indexOf("('") + 2, checkStr.lastIndexOf('\'')).split("','")
        val code = arr[0].toInt()
        val needCaptcha = code == 1
        val qqVc = QqVc(qqApp.appId, qqApp.daId, cookie, loginSig, ptdRvs!!, redirectUrl, enRedirectUrl, xuiUrl)
        qqVc.code = code
        qqVc.needCaptcha = needCaptcha
        qqVc.sid = arr[6]
        if (!needCaptcha) {
            qqVc.randStr = arr[1]
            qqVc.ticket = ptVfSession!!
        }
        return qqVc
    }

    private suspend fun login(qq: Long, password: String, qqVc: QqVc): CommonResult<QqLoginPojo> {
        val encryptPassword = QqUtils.encryptPassword(qq, password, qqVc.randStr)
        var url = "https://ssl.ptlogin2.qq.com/login?u=$qq&verifycode=${qqVc.randStr}&pt_vcode_v1=${qqVc.code}&pt_verifysession_v1=${qqVc.ticket}&p=$encryptPassword&pt_randsalt=2&u1=${qqVc.enRedirectUrl}&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=3-14-${System.currentTimeMillis()}&js_ver=21110815&js_type=1&login_sig=${qqVc.loginSig}&pt_uistyle=40&aid=${qqVc.appId}&daid=${qqVc.daId}&ptdrvs=${qqVc.ptdRvs}&sid=${qqVc.sid}&"
        if (qqVc.smsCode.isNotEmpty()) {
            url += "&pt_sms_code=${qqVc.smsCode}"
            qqVc.cookie = qqVc.cookie + "pt_sms=${qqVc.smsCode}; "
        }
        val response = OkHttpKtUtils.get(url, OkUtils.headers(qqVc.cookie, "https://xui.ptlogin2.qq.com/", UA.PC))
        val resultCookie = OkUtils.cookie(response)
        val str = OkUtils.str(response)
        val result = QqUtils.getResultUrl(str)
        return when (result.code) {
            200 -> {
                val qqLoginPojo = QqLoginPojo()
                qqLoginPojo.qq = qq
                qqLoginPojo.sKey = OkUtils.cookie(resultCookie, "skey")!!
                qqLoginPojo.superKey = OkUtils.cookie(resultCookie, "superkey")!!
                qqLoginPojo.superToken = OkUtils.cookie(resultCookie, "supertoken")!!
                val otherKey = QqUtils.getKey(result.data)
                qqLoginPojo.psKey = otherKey["p_skey"]!!
                qqLoginPojo.pt4Token = otherKey["pt4_token"] ?: ""
                qqLoginPojo.ptOauthToken = otherKey["pt_oauth_token"] ?: ""
                qqLoginPojo.ptLoginType = otherKey["pt_login_type"] ?: ""
                CommonResult.success(qqLoginPojo)
            }
            502 -> CommonResult.failure("登录失败，请稍后再试")
            10009 -> {
                val ptdRvs = OkUtils.cookie(resultCookie, "ptdrvs")
                val ticket = OkUtils.cookie(resultCookie, "pt_sms_ticket")
                qqVc.cookie = qqVc.cookie.replace(qqVc.ptdRvs, "$ptdRvs; pt_sms_ticket=$ticket; ")
                sendSms(qqVc.appId, qq, ticket!!, qqVc.cookie)
                throw QqLoginNeedSmsException(qqVc, result.data ?: "")
            }
            else -> CommonResult.failure(result.message)
        }
    }

    private suspend fun sendSms(appId: Long, qq: Long, smsTicket: String, cookie: String) {
        OkHttpKtUtils.get("https://ssl.ptlogin2.qq.com/send_sms_code?bkn=&uin=$qq&aid=$appId&pt_sms_ticket=$smsTicket",
            OkUtils.cookie(cookie)).close()
    }

    suspend fun login(qq: Long, password: String, qqApp: QqApp = QqApp(549000912, 5), url: String = "https://qzs.qq.com/qzone/v5/loginsucc.html?para=izone"): CommonResult<QqLoginPojo> {
        val qqVc = checkVc(qqApp, qq, url)
        if (qqVc.needCaptcha) {
            val result = TenCentCaptchaUtils.identify(qqVc.appId, qqVc.xuiUrl, qqVc.sid)
            if (result.failure()) return CommonResult.failure(result.message)
            val tencentCaptcha = result.data()
            qqVc.ticket = tencentCaptcha.ticket
            qqVc.randStr = tencentCaptcha.randStr
        }
        return login(qq, password, qqVc)
    }

    suspend fun loginBySms(qq: Long, password: String, qqVc: QqVc): CommonResult<QqLoginPojo> {
        return login(qq, password, qqVc)
    }


}