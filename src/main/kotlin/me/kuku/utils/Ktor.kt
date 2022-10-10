package me.kuku.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
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
            jackson()
        }
    }
}

fun FormBuilder.append(key: String, byteArray: ByteArray, filename: String, contentType: String = "application/octet-stream") {
    this.append(key, byteArray, Headers.build {
        append(HttpHeaders.ContentType, contentType)
        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
    })
}