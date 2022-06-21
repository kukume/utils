@file:Suppress("DuplicatedCode", "unused")

package me.kuku.utils

import me.kuku.pojo.CommonResult
import me.kuku.pojo.QqConnectLoginNeedSmsException
import me.kuku.pojo.UA

object QqPasswordConnectLoginUtils {

    data class QqVc(
        val appId: Long,
        val daId: Int,
        val ptAid: Long,
        val h5sig: String,
        var cookie: String,
        val ptdRvs: String,
        val redirectUrl: String,
        val xuiUrl: String,
        val time: String,

        var code: Int = 0,
        var needCaptcha: Boolean = false,
        var randomStr: String = "",
        var ticket: String = "",
        var sid: String = "",

        var smsCode: String = ""
    )

    private suspend fun checkVc(qq: Long, ptAid: Long, redirectUrl: String ,state: String): QqVc {
        val enRedirectUrl = redirectUrl.toUrlEncode()
        val graphStr = OkHttpKtUtils.getStr("https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=$ptAid&redirect_uri=$enRedirectUrl&scope=get_user_info&state=$state",
            OkUtils.ua(UA.QQ))
        val enUrl = MyUtils.regex("content=\"1;url=", "\"", graphStr)!!
        val xuiUrl = enUrl.replace("&#61;", "=").replace("&amp;", "&")
        val xuiResponse = OkHttpUtils.get(xuiUrl, OkUtils.ua(UA.QQ))
        xuiResponse.close()
        val xuiCookie = OkUtils.cookie(xuiResponse)
        val appId = MyUtils.regex("appid=", "&", xuiUrl)!!
        val daId = MyUtils.regex("daid=", "&", xuiUrl)!!
        val h5sig = MyUtils.regex("h5sig=", "&", xuiUrl)!!
        val time = MyUtils.regex("time=", "&", xuiUrl)!!
        val checkResponse = OkHttpKtUtils.get("https://xui.ptlogin2.qq.com/ssl/check?pt_tea=2&uin=$qq&appid=$appId&ptlang=2052&regmaster=&pt_uistyle=35&r=0.${MyUtils.randomNum(16)}",
            OkUtils.headers(xuiCookie, xuiUrl, UA.PC))
        val checkCookie = OkUtils.cookie(checkResponse)
        val cookie = xuiCookie + checkCookie
        val ptdRvs = OkUtils.cookie(checkCookie, "ptdrvs")
        val ptVfSession = OkUtils.cookie(checkCookie, "ptvfsession")
        val checkStr = OkUtils.str(checkResponse)
        val arr = checkStr.substring(checkStr.indexOf("('") + 2, checkStr.lastIndexOf('\'')).split("','")
        val code = arr[0].toInt()
        val needCaptcha = code == 1
        val qqVc = QqVc(appId.toLong(), daId.toInt(), ptAid, h5sig, cookie, ptdRvs!!, redirectUrl, xuiUrl, time)
        qqVc.code = code
        qqVc.needCaptcha = needCaptcha
        qqVc.sid = arr[6]
        if (!needCaptcha) {
            qqVc.randomStr = arr[1]
            qqVc.ticket = ptVfSession!!
        }
        return qqVc
    }

    private suspend fun login(qq: Long, password: String, qqVc: QqVc, state: String): CommonResult<String> {
        val idt = System.currentTimeMillis() / 1000 - 5
        val encryptPassword = QqUtils.encryptPassword(qq, password, qqVc.randomStr)
        val redirectUrl = qqVc.redirectUrl.toUrlEncode().toUrlEncode()
        var uri = "https://xui.ptlogin2.qq.com/ssl/pt_open_login?openlogin_data=which%3D%26refer_cgi%3Dauthorize%26response_type%3Dcode%26client_id%3D${qqVc.ptAid}%26state%3D$state%26display%3D%26openapi%3D1010_1011%26switch%3D0%26src%3D1%26sdkv%3Dv1.0%26sdkp%3Dpcweb%26tid%3D$idt%26pf%3D%26need_pay%3D0%26browser%3D0%26browser_error%3D%26serial%3D%26token_key%3D%26redirect_uri%3D$redirectUrl%26sign%3D%26time%3D${qqVc.time}%26status_version%3D%26status_os%3D%26status_machine%3D%26page_type%3D1%26has_auth%3D1%26update_auth%3D1%26auth_time%3D${System.currentTimeMillis()}%26loginfrom%3D%26h5sig%3D${qqVc.h5sig}%26loginty%3D3%26&ptdrvs=${qqVc.ptdRvs}&pt_vcode_v1=${qqVc.code}&pt_verifysession_v1=${qqVc.ticket}&verifycode=${qqVc.randomStr}&u=$qq&p=$encryptPassword&pt_randsalt=2&ptlang=2052&low_login_enable=0&u1=https%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=${qqVc.appId}&daid=${qqVc.daId}&pt_3rd_aid=${qqVc.ptAid}&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&sid=${qqVc.sid}&o1vId=&"
        if (qqVc.smsCode.isNotEmpty()) {
            uri += "&pt_sms_code=${qqVc.smsCode}"
            qqVc.cookie = qqVc.cookie + "pt_sms=${qqVc.smsCode}; "
        }
        val response = OkHttpKtUtils.get(uri, OkUtils.headers("${qqVc.cookie}idt=$idt; ", qqVc.xuiUrl, UA.QQ))
        val str = OkUtils.str(response)
        val result = QqUtils.getResultUrl(str)
        return when (result.code) {
            200 -> CommonResult.success(result.data)
            502 -> CommonResult.failure("登录失败，请稍后再试")
            10009 -> {
                val resultCookie = OkUtils.cookie(response)
                val ptdRvs = OkUtils.cookie(resultCookie, "ptdrvs")
                val ticket = OkUtils.cookie(resultCookie, "pt_sms_ticket")
                qqVc.cookie = qqVc.cookie.replace(qqVc.ptdRvs, "$ptdRvs; pt_sms_ticket=$ticket; ")
                sendSms(qq, qqVc.appId, ticket!!, qqVc.cookie)
                throw QqConnectLoginNeedSmsException(qqVc, result.data ?: "")
            }
            else -> CommonResult.failure(result.message)
        }
    }

    private suspend fun sendSms(qq: Long, appId: Long, ptSmsTicket: String, cookie: String) {
        OkHttpKtUtils.get("https://ui.ptlogin2.qq.com/ssl/send_sms_code?bkn=&uin=$qq&aid=$appId&pt_sms_ticket=$ptSmsTicket",
            OkUtils.cookie(cookie))
    }

    suspend fun login(qq: Long, password: String, ptAid: Long, url: String, state: String = ""): CommonResult<String> {
        val qqVc = checkVc(qq, ptAid, url, state)
        if (qqVc.needCaptcha) {
            val result = TenCentCaptchaUtils.identify(qqVc.appId, qqVc.xuiUrl, qqVc.sid)
            if (result.failure()) return CommonResult.failure(result.message)
            val tencentCaptcha = result.data()
            qqVc.ticket = tencentCaptcha.ticket
            qqVc.randomStr = tencentCaptcha.randStr
        }
        return login(qq, password, qqVc, state)
    }

    suspend fun loginBySms(qq: Long, password: String, qqVc: QqVc, state: String = ""): CommonResult<String> {
        return login(qq, password, qqVc, state)
    }

}