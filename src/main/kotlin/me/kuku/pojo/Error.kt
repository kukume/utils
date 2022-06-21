@file:Suppress("unused")

package me.kuku.pojo

import me.kuku.utils.QqPasswordConnectLoginUtils
import me.kuku.utils.QqPasswordLoginUtils

class QqLoginNeedSmsException(val qqVc: QqPasswordLoginUtils.QqVc, val phone: String): RuntimeException()

class QqConnectLoginNeedSmsException(val qqVc: QqPasswordConnectLoginUtils.QqVc, val phone: String): RuntimeException()

class MissingRegexResultException(override val message: String = ""): RuntimeException(message)