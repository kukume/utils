package me.kuku

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import me.kuku.utils.client
import org.junit.jupiter.api.Test

class KotlinTest {

    @Test
    fun test1() {
        runBlocking {
            val httpResponse = client.get("https://www.baidu.com") {
                headers {
                    accept(ContentType.Application.OctetStream)
                }
            }
            println(httpResponse.request.headers)
        }
    }

}
