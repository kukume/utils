@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.kuku.utils

import com.fasterxml.jackson.databind.JsonNode
import me.kuku.pojo.UA
import okhttp3.*
import okio.ByteString
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.Proxy
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object OkHttpUtils {

    @JvmStatic
    var timeOut = 30L

    @JvmStatic
    var okhttpClient: OkHttpClient? = null

    @JvmStatic
    var proxy: Proxy? = null

    @Synchronized
    fun okhttpClient(): OkHttpClient {
        if (okhttpClient == null) {
            val trustAllCert = object : X509TrustManager {
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
            val trustAllCerts = arrayOf(trustAllCert)
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(timeOut, TimeUnit.SECONDS)
                .readTimeout(timeOut, TimeUnit.SECONDS)
            if (proxy != null) {
                builder.proxy(proxy)
            }
            okhttpClient = builder.build()
        }
        return okhttpClient!!
    }

    @JvmStatic
    fun addHeaders(map: Map<String, String>): Headers {
        val builder = Headers.Builder()
        map.forEach(builder::add)
        return builder.build()
    }

    private fun defaultHeaders(): Headers {
        return addHeaders(mapOf("user-agent" to UA.PC.value))
    }

    private fun x(request: Request): Response {
        return okhttpClient().newCall(request).execute()
    }

    @JvmStatic
    @JvmOverloads
    fun get(url: String, headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).headers(headers).build()
        return x(request)
    }

    private fun callResponse(request: Request): CallResponse {
        val callResponse = CallResponse()
        okhttpClient().newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                callResponse.runFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callResponse.runResponse(response)
            }
        })
        return callResponse
    }

    @JvmStatic
    @JvmOverloads
    fun getAsync(url: String, headers: Headers = defaultHeaders()): CallResponse {
        val request = Request.Builder().url(url).headers(headers).build()
        return callResponse(request)
    }

    @JvmStatic
    fun getAsync(url: String, map: Map<String, String>): CallResponse {
        return getAsync(url, addHeaders(map))
    }

    @JvmStatic
    fun get(url: String, map: Map<String, String>): Response {
        return get(url, addHeaders(map))
    }

    private fun mapToFormBody(map: Map<String, String>): FormBody {
        val builder = FormBody.Builder()
        map.forEach(builder::add)
        return builder.build()
    }

    @JvmStatic
    @JvmOverloads
    fun post(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).post(requestBody).headers(headers).build()
        return x(request)
    }

    @JvmStatic
    fun post(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return post(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun post(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return post(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun post(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return post(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun postAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): CallResponse {
        val request = Request.Builder().url(url).post(requestBody).headers(headers).build()
        return callResponse(request)
    }

    @JvmStatic
    fun postAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): CallResponse {
        return postAsync(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun postAsync(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): CallResponse {
        return postAsync(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun postAsync(url: String, map: Map<String, String>, headerMap: Map<String, String>): CallResponse {
        return postAsync(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun put(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).put(requestBody).headers(headers).build()
        return x(request)
    }

    @JvmStatic
    fun put(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return put(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun put(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return put(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun put(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return put(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun putAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): CallResponse {
        val request = Request.Builder().url(url).put(requestBody).headers(headers).build()
        return callResponse(request)
    }

    @JvmStatic
    fun putAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): CallResponse {
        return putAsync(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun putAsync(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): CallResponse {
        return putAsync(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun putAsync(url: String, map: Map<String, String>, headerMap: Map<String, String>): CallResponse {
        return putAsync(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun delete(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).delete(requestBody).headers(headers).build()
        return x(request)
    }

    @JvmStatic
    fun delete(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return delete(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun delete(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return delete(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun delete(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return post(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun deleteAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): CallResponse {
        val request = Request.Builder().url(url).delete(requestBody).headers(headers).build()
        return callResponse(request)
    }

    @JvmStatic
    fun deleteAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): CallResponse {
        return deleteAsync(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun deleteAsync(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): CallResponse {
        return deleteAsync(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun deleteAsync(url: String, map: Map<String, String>, headerMap: Map<String, String>): CallResponse {
        return deleteAsync(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun patch(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).patch(requestBody).headers(headers).build()
        return x(request)
    }

    @JvmStatic
    fun patch(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return patch(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun patch(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return patch(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun patch(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return patch(url, map, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun patchAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): CallResponse {
        val request = Request.Builder().url(url).patch(requestBody).headers(headers).build()
        return callResponse(request)
    }

    @JvmStatic
    fun patchAsync(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): CallResponse {
        return patchAsync(url, requestBody, addHeaders(headersMap))
    }

    @JvmStatic
    @JvmOverloads
    fun patchAsync(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): CallResponse {
        return patchAsync(url, mapToFormBody(map), headers)
    }

    @JvmStatic
    fun patchAsync(url: String, map: Map<String, String>, headerMap: Map<String, String>): CallResponse {
        return patchAsync(url, map, addHeaders(headerMap))
    }

    // ---------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------

    @JvmStatic
    @JvmOverloads
    fun getStr(url: String, headers: Headers = defaultHeaders()): String {
        val response = get(url, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun getStr(url: String, map: Map<String, String>): String {
        val response = get(url, map)
        return OkUtils.str(response)
    }

    @JvmStatic
    @JvmOverloads
    fun getJson(url: String, headers: Headers = defaultHeaders()): JsonNode {
        val response = get(url, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun getJson(url: String, map: Map<String, String>): JsonNode {
        val response = get(url, map)
        return OkUtils.json(response)
    }

    @JvmStatic
    @JvmOverloads
    fun getJsonp(url: String, headers: Headers = defaultHeaders()): JsonNode {
        val response = get(url, headers)
        return OkUtils.jsonp(response)
    }

    @JvmStatic
    fun getJsonp(url: String, map: Map<String, String>): JsonNode {
        val response = get(url, map)
        return OkUtils.jsonp(response)
    }

    @JvmStatic
    @JvmOverloads
    fun getBytes(url: String, headers: Headers = defaultHeaders()): ByteArray {
        val response = get(url, headers)
        return OkUtils.bytes(response)
    }

    @JvmStatic
    fun getBytes(url: String, map: Map<String, String>): ByteArray {
        val response = get(url, map)
        return OkUtils.bytes(response)
    }

    @JvmStatic
    @JvmOverloads
    fun getByteStream(url: String, headers: Headers = defaultHeaders()): InputStream {
        val response = get(url, headers)
        return OkUtils.byteStream(response)
    }

    @JvmStatic
    fun getByteStream(url: String, map: Map<String, String>): InputStream {
        val response = get(url, map)
        return OkUtils.byteStream(response)
    }

    @JvmStatic
    @JvmOverloads
    fun getByteString(url: String, headers: Headers = defaultHeaders()): ByteString {
        val response = get(url, headers)
        return OkUtils.byteString(response)
    }

    @JvmStatic
    fun getByteString(url: String, map: Map<String, String>): ByteString {
        val response = get(url, map)
        return OkUtils.byteString(response)
    }

    // ----------------------------------------------------------------------------------------------------------

    @JvmStatic
    @JvmOverloads
    fun postStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = post(url, requestBody, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun postStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        val response = post(url, requestBody, addHeaders(headerMap))
        return OkUtils.str(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = post(url, map, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun postStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = post(url, map, headerMap)
        return OkUtils.str(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, requestBody, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun postJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        val response = post(url, requestBody, addHeaders(headerMap))
        return OkUtils.json(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, map, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun postJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = post(url, map, headerMap)
        return OkUtils.json(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postJsonp(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, requestBody, headers)
        return OkUtils.jsonp(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postJsonp(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, map, headers)
        return OkUtils.jsonp(response)
    }

    @JvmStatic
    fun postJsonp(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = post(url, map, headerMap)
        return OkUtils.jsonp(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postBytes(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): ByteArray {
        val response = post(url, requestBody, headers)
        return OkUtils.bytes(response)
    }

    @JvmStatic
    fun postBytes(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): ByteArray {
        val response = post(url, requestBody, addHeaders(headerMap))
        return OkUtils.bytes(response)
    }

    @JvmStatic
    @JvmOverloads
    fun postBytes(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): ByteArray {
        val response = post(url, map, headers)
        return OkUtils.bytes(response)
    }

    @JvmStatic
    fun postBytes(url: String, map: Map<String, String>, headerMap: Map<String, String>): ByteArray {
        val response = post(url, map, headerMap)
        return OkUtils.bytes(response)
    }

    // ---------------------------------------------------------------------------------------------------------------

    @JvmStatic
    @JvmOverloads
    fun putStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = put(url, requestBody, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun putStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        return putStr(url, requestBody, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun putStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = put(url, map, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun putStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = put(url, map, headerMap)
        return OkUtils.str(response)
    }

    @JvmStatic
    @JvmOverloads
    fun putJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = put(url, requestBody, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun putJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        return putJson(url, requestBody, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun putJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = put(url, map, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun putJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = put(url, map, headerMap)
        return OkUtils.json(response)
    }

    // ----------------------------------------------------------------------------------------------------------------

    @JvmStatic
    @JvmOverloads
    fun deleteStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = delete(url, requestBody, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun deleteStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        return deleteStr(url, requestBody, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun deleteStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = delete(url, map, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun deleteStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = delete(url, map, headerMap)
        return OkUtils.str(response)
    }

    @JvmStatic
    @JvmOverloads
    fun deleteJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = delete(url, requestBody, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun deleteJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        return deleteJson(url, requestBody, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun deleteJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = delete(url, map, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun deleteJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = delete(url, map, headerMap)
        return OkUtils.json(response)
    }

    // ---------------------------------------------------------------------------------------------------------------

    @JvmStatic
    @JvmOverloads
    fun patchStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = patch(url, requestBody, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun patchStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        return patchStr(url, requestBody, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun patchStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = patch(url, map, headers)
        return OkUtils.str(response)
    }

    @JvmStatic
    fun patchStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = patch(url, map, headerMap)
        return OkUtils.str(response)
    }

    @JvmStatic
    @JvmOverloads
    fun patchJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = patch(url, requestBody, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun patchJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        return patchJson(url, requestBody, addHeaders(headerMap))
    }

    @JvmStatic
    @JvmOverloads
    fun patchJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = patch(url, map, headers)
        return OkUtils.json(response)
    }

    @JvmStatic
    fun patchJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = patch(url, map, headerMap)
        return OkUtils.json(response)
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ================== get请求把文件保存在本地 ===============================

    @JvmStatic
    @JvmOverloads
    fun getFile(url: String, path: Path, headers: Headers = defaultHeaders()): File {
        val response = get(url, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    @JvmOverloads
    fun getFile(url: String, file: File,  headers: Headers = defaultHeaders()): File {
        val response = get(url, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    fun getFile(url: String, path: Path, headers: Map<String, String>): File {
        val response = get(url, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    fun getFile(url: String, file: File, headers: Map<String, String>): File {
        val response = get(url, headers)
        return OkUtils.file(response, file)
    }

    // ================== post请求把文件保存在本地 ===============================

    @JvmStatic
    @JvmOverloads
    fun postFile(url: String, path: Path, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    @JvmOverloads
    fun postFile(url: String, file: File, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    fun postFile(url: String, path: Path, requestBody: RequestBody = FormBody.Builder().build(), headers: Map<String, String>): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    fun postFile(url: String, file: File, requestBody: RequestBody = FormBody.Builder().build(), headers: Map<String, String>): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    @JvmOverloads
    fun postFile(url: String, path: Path, map: Map<String, String>, headers: Headers = defaultHeaders()): File {
        val response = post(url, map, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    @JvmOverloads
    fun postFile(url: String, file: File, map: Map<String, String>, headers: Headers = defaultHeaders()): File {
        val response = post(url, map, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    fun postFile(url: String, path: Path, map: Map<String, String>, headerMap: Map<String, String>): File {
        val response = post(url, map, headerMap)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    fun postFile(url: String, file: File, map: Map<String, String>, headerMap: Map<String, String>): File {
        val response = post(url, map, headerMap)
        return OkUtils.file(response, file)
    }
}

class CallResponse {

    private var responseBlock: (OkHttpUtils.(Response) -> Unit)? = null
    private var failureBlock: (OkHttpUtils.(IOException) -> Unit)? = null
    private var responseConsumer: Consumer<Response>? = null
    private var failureConsumer: Consumer<IOException>? = null

    fun onResponse(block: OkHttpUtils.(Response) -> Unit): CallResponse {
        this.responseBlock = block
        return this
    }

    fun onResponse(consumer: Consumer<Response>): CallResponse {
        this.responseConsumer = consumer
        return this
    }

    fun onFailure(block: OkHttpUtils.(IOException) -> Unit): CallResponse {
        this.failureBlock = block
        return this
    }

    fun onFailure(consumer: Consumer<IOException>): CallResponse {
        this.failureConsumer = consumer
        return this
    }

    fun runResponse(response: Response) {
        responseBlock?.invoke(OkHttpUtils, response)
        responseConsumer?.accept(response)
    }

    fun runFailure(e: IOException) {
        failureBlock?.invoke(OkHttpUtils, e)
        failureConsumer?.accept(e)
    }

}