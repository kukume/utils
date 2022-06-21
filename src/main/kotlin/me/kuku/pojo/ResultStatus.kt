@file:Suppress("unused")

package me.kuku.pojo

enum class ResultStatus(val code: Int, val message: String) {
    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    PARAM_ERROR(501, "参数异常"),
    DATA_EXISTS(502, "数据已存在"),
    DATA_NOT_EXISTS(503, "数据不存在"),
    NOT_SCANNED(0, "二维码未扫描"),
    QRCODE_NOT_SCANNED(0, "二维码未扫描"),
    EXPIRED(505, "二维码已失效"),
    QRCODE_EXPIRED(505, "二维码已失效"),
    CONFIG_ERROR(506, "配置文件错误"),
    SCANNED(507, "二维码已扫描"),
    COOKIE_EXPIRED(508, "cookie已失效"),
    DYNAMIC_NOT_FOUNT(509, "动态没找到"),
    QRCODE_IS_SCANNED(0, "二维码已扫描"),
    QRCODE_IS_REFUSE(511, "二维码已被拒绝"),
    LOGIN_FAIL(512, "账号或密码错误"),
    NOT_LOGIN(513, "未登录"),
    NOT_AUTH(514, "无权限"),
    ALREADY_REGISTER(515, "已注册"),
    NOT_BIND(516, "没有绑定"),
    TYPE_ERROR(517, "类型不存在"),
    OLD_PASSWORD_ERROR(518, "原密码错误"),
    QQ_IS_BIND(519, "该qq已被绑定")

    ;

    fun toResult(): CommonResult<Any> {
        return CommonResult.failure(this.message)
    }

    fun <T> toResult(data: T): CommonResult<T> {
        return CommonResult.failure(this.message, data)
    }
}
