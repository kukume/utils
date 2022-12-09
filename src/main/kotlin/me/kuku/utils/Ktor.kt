package me.kuku.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

internal val trustAllCert = object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
        return arrayOf()
    }

    override fun checkClientTrusted(
        certs: Array<java.security.cert.X509Certificate>,
        authType: String
    ) {
    }

    override fun checkServerTrusted(
        certs: Array<java.security.cert.X509Certificate>,
        authType: String
    ) {
    }
}
internal val trustAllCerts = arrayOf(trustAllCert)
internal val sslContext = SSLContext.getInstance("SSL").also { it.init(null, trustAllCerts, java.security.SecureRandom()) }
internal val sslSocketFactory = sslContext.socketFactory

private var ktorProxy: Proxy? = null

fun setKtorProxy(proxyParam: Proxy) {
    ktorProxy = proxyParam
}

private fun ObjectMapper.config() {
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    registerModule(JavaTimeModule())
    registerModule(kotlinModule())
}

val client by lazy {
    HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(false)
                followSslRedirects(false)
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                hostnameVerifier { _, _ -> true }
                if (ktorProxy != null) {
                    proxy(ktorProxy)
                }
            }
        }

        install(ContentNegotiation) {
            jackson {
                config()
            }
        }

        install(WebSockets) {
            contentConverter = JacksonWebsocketContentConverter(Jackson.objectMapper)
        }
    }
}

fun FormBuilder.append(key: String, byteArray: ByteArray, filename: String, contentType: String = "application/octet-stream") {
    this.append(key, byteArray, Headers.build {
        append(HttpHeaders.ContentType, contentType)
        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
    })
}

fun HttpResponse.cookie(): String {
    val cookies = this.setCookie()
    val sb = StringBuilder()
    for (cookie in cookies) {
        sb.append("${cookie.name}=${cookie.value}; ")
    }
    return sb.toString()
}

fun HttpMessageBuilder.origin(content: String): Unit = headers.set(HttpHeaders.Origin, content)

fun HttpMessageBuilder.referer(content: String): Unit = headers.set(HttpHeaders.Referrer, content)

fun HttpMessageBuilder.cookieString(content: String): Unit = headers.set(HttpHeaders.Cookie, content)

fun HttpRequestBuilder.setJsonBody(content: Any) {
    contentType(ContentType.Application.Json)
    setBody(content)
}

fun HttpRequestBuilder.setFormDataContent(builder: ParametersBuilder.() -> Unit) {
    setBody(FormDataContent(Parameters.build { builder() }))
}

fun ParametersBuilder.append(map: Map<String, String>) {
    map.forEach { (k, v) ->
        this.append(k, v)
    }
}
