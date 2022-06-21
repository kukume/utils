@file:Suppress("unused")

package me.kuku.pojo

import com.alibaba.fastjson.annotation.JSONField
import me.kuku.utils.QqUtils

data class QqLoginPojo(
    var qq: Long = 0,
    var sKey: String = "",
    var psKey: String = "",
    var superKey: String = "",
    var superToken: String = "",
    var pt4Token: String = "",
    var ptOauthToken: String = "",
    var ptLoginType: String = "",
) {
    @get:JSONField(serialize = false)
    val cookie: String
        get() = String.format("pt2gguin=o0%s; uin=o0%s; skey=%s; ", qq, qq, sKey)

    @JSONField(serialize = false)
    fun getCookie(psKey: String?): String {
        return String.format("%sp_skey=%s; p_uin=o0%s;", cookie, psKey, qq)
    }

    @get:JSONField(serialize = false)
    val cookieWithPs: String
        get() = String.format("%sp_skey=%s; p_uin=o0%s; ", cookie, psKey, qq)

    @get:JSONField(serialize = false)
    val cookieWithSuper: String
        get() = String.format("superuin=o0%s; superkey=%s; supertoken=%s; ", qq, superKey, superToken)

    @get:JSONField(serialize = false)
    val gtk: String
        get() = java.lang.String.valueOf(QqUtils.getGTK(sKey))

    @JSONField(serialize = false)
    fun getGtk(psKey: String): String {
        return java.lang.String.valueOf(QqUtils.getGTK(psKey))
    }

    @get:JSONField(serialize = false)
    val gtk2: String
        get() = QqUtils.getGTK2(sKey)

    @get:JSONField(serialize = false)
    val gtkP: String
        get() = java.lang.String.valueOf(QqUtils.getGTK(psKey))

    @get:JSONField(serialize = false)
    val token: String
        get() = java.lang.String.valueOf(QqUtils.getToken(superToken))

    @get:JSONField(serialize = false)
    val token2: String
        get() = java.lang.String.valueOf(QqUtils.getToken2(superToken))

    @get:JSONField(serialize = false)
    val authorizeCookie: String
        get() = String.format(
            "p_skey=%s; p_uin=o0%s; pt_oauth_token=%s; pt_login_type=%s; pt4_token=%s; ",
            psKey, qq, ptOauthToken, ptLoginType, pt4Token
        )
}