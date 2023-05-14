package net.bnb1.commons.http

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import net.bnb1.commons.lifecycle.LifecycleException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.net.ConnectException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Suppress("BlockingMethodInNonBlockingContext")
class HttpServerTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val port = ServerSocket(0).use { it.localPort }
    val httpServer = HttpServer(port = port, connectionTimeout = 100)
    val client = OkHttpClient.Builder().build()

    fun request(path: String) = Request.Builder().url("http://localhost:$port$path")

    afterEach {
        if (httpServer.running) {
            httpServer.stop()
        }
    }

    context("HTTP server stopped") {

        test("Start container") {
            httpServer.running shouldBe false
            httpServer.start()
            httpServer.running shouldBe true
        }

        test("Start container multiple times") {
            httpServer.start()
            shouldThrow<LifecycleException> { httpServer.start() }
        }

        test("Stop container") {
            shouldThrow<LifecycleException> { httpServer.stop() }
        }

        test("GET request") {
            val request = request("/test").get().build()
            shouldThrow<ConnectException> { client.newCall(request).execute() }
        }
    }

    context("HTTP server started") {

        httpServer.start()

        test("Stop container") {
            httpServer.stop()
            httpServer.running shouldBe false
        }

        context("GET requests") {

            test("Use path string") {
                httpServer.get("/test") { it.copy(body = "OK") }

                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Use path pattern") {
                httpServer.get(Regex(("/test.*"))) { it.copy(body = "OK") }

                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Use path string (infix)") {
                httpServer GET "/test" by { it.copy(body = "OK") }

                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Use path pattern (infix)") {
                httpServer GET Regex("/test.*") by { it.copy(body = "OK") }

                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Register multiple handlers") {
                httpServer.get("/test") { it.copy(body = "1") }
                httpServer.get("/test") { it.copy(body = "2") }

                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "1"
            }

            test("Not found") {
                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.NOT_FOUND.code
            }

            test("Method not allowed") {
                httpServer.post(Regex(("/test.*"))) { it.copy(body = "OK") }

                val request = request("/test").get().build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.METHOD_NOT_ALLOWED.code
            }
        }

        context("POST requests") {

            test("Use path string") {
                httpServer.post("/test") { it.copy(body = "OK") }

                val request = request("/test").post(EMPTY_REQUEST).build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Use path pattern") {
                httpServer.post(Regex(("/test.*"))) { it.copy(body = "OK") }

                val request = request("/test").post(EMPTY_REQUEST).build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Use path string (infix)") {
                httpServer POST "/test" by { it.copy(body = "OK") }

                val request = request("/test").post(EMPTY_REQUEST).build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Use path pattern (infix)") {
                httpServer POST Regex("/test.*") by { it.copy(body = "OK") }

                val request = request("/test").post(EMPTY_REQUEST).build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Post data") {
                httpServer.post("/test") { it.copy(body = "OK") }

                val body = "Test".toRequestBody()
                val request = request("/test").post(body).build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }

            test("Post lots of data") {
                httpServer.post("/test") { it.copy(body = "OK") }

                val body = "Test".repeat(500).toRequestBody()
                val request = request("/test").post(body).build()
                val response = client.newCall(request).execute()
                response.code shouldBe HttpStatus.OK.code
                response.body?.string() shouldBe "OK"
            }
        }

        context("Miscellaneous") {

            test("Send request with timeout") {
                val socket = Socket("localhost", port)
                socket.getOutputStream().write("GET /te".toByteArray())
                delay(300)
                shouldThrow<SocketException> {
                    socket.getOutputStream().write('s'.code)
                    socket.getOutputStream().write('t'.code)
                }
            }
        }
    }
})
