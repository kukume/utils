package me.kuku.utils

import com.fasterxml.jackson.module.kotlin.contains
import me.kuku.pojo.CommonResult
import me.kuku.pojo.UA

object TenCentCaptchaUtils {

    private const val ua = "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzk1LjAuNDYzOC42OSBTYWZhcmkvNTM3LjM2"

    private suspend fun captchaUrl(appId: Long, sid: String, capCd: String, qq: String, refererUrl: String): String {
        val preHandUrl = "https://t.captcha.qq.com/cap_union_prehandle?aid=$appId&protocol=https&accver=1&showtype=embed&ua=$ua&noheader=1&fb=1&aged=0&enableAged=0&enableDarkMode=0&sid=$sid&grayscale=1&clientype=2&cap_cd=$capCd&uid=$qq&wxLang=&lang=zh-CN&entry_url=${refererUrl.toUrlEncode()}&js=%2Ftcaptcha-frame.85d7a77d.js&subsid=1&callback=_aq_353052&sess="
        val jsonNode = OkHttpKtUtils.getJsonp(preHandUrl, OkUtils.headers("", "https://xui.ptlogin2.qq.com/", UA.PC))
        val sess = jsonNode["sess"].asText()
        val newSid = jsonNode["sid"].asText()
        val createIframeStart = System.currentTimeMillis()
        val rnd = MyUtils.randomNum(6)
        return "https://t.captcha.qq.com/cap_union_new_show?aid=$appId&protocol=https&accver=1&showtype=embed&ua=$ua&noheader=1&fb=1&aged=0&enableAged=0&enableDarkMode=0&sid=$newSid&grayscale=1&clientype=2&sess=$sess&fwidth=0&wxLang=&tcScale=1&uid=$qq&cap_cd=$capCd&rnd=$rnd&prehandleLoadTime=23&createIframeStart=$createIframeStart&subsid=2"
    }

    suspend fun identify(url: String): CommonResult<TencentCaptcha> {
        val jsonNode = OkHttpKtUtils.postJson("https://api.kukuqaq.com/captcha", mapOf("url" to url))
        return if (!jsonNode.contains("code")) {
            CommonResult.success(TencentCaptcha(jsonNode["ticket"].asText(), jsonNode["randStr"].asText()))
        } else CommonResult.failure(jsonNode["message"].asText())
    }

    suspend fun identify(appId: Long, refererUrl: String, sid: String = "", capCd: String = "", qq: String = ""): CommonResult<TencentCaptcha> {
        val url = captchaUrl(appId, sid, capCd, qq, refererUrl)
        return identify(url)
    }

}

data class TencentCaptcha(val ticket: String, val randStr: String)