package com.dreamcode

import com.dreamcode.domain.models.AuthData
import com.dreamcode.presentation.*
import com.dreamcode.presentation.util.BaseResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthIntegrationTest {

    private fun TestApplicationBuilder.setupApp() {
        environment {
            config = MapApplicationConfig(
                "storage.driverClassName" to "org.h2.Driver",
                "storage.jdbcURL" to "jdbc:h2:mem:auth_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1",
                "storage.user" to "root",
                "storage.password" to "",
                "jwt.secret" to "test-secret",
                "jwt.issuer" to "http://0.0.0.0:8080",
                "jwt.audience" to "notes-audience",
                "email.apiKey" to "test-api-key",
                "email.from" to "test@example.com"
            )
        }
        application {
            configureDatabases()
            configureHTTP()
            configureSecurity()
            configureSerialization()
            configureRouting()
        }
    }

    @Test
    fun testAuthFlow() = testApplication {
        setupApp()
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val testEmail = "test-${System.currentTimeMillis()}@example.com"
        val testName = "Test User"
        val testPassword = "Password123!"

        // 1. Register
        val regResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to testName, "email" to testEmail, "password" to testPassword))
        }
        assertEquals(HttpStatusCode.Created, regResponse.status)
        val regBody = regResponse.body<BaseResponse<AuthData>>()
        assertTrue(regBody.status)
        assertNotNull(regBody.data?.accessToken)
        assertEquals(testEmail, regBody.data?.user?.email)

        // 2. Login (Should send OTP)
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to testEmail, "password" to testPassword))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        // 3. Test Error Cases
        
        // Case: Login with non-existent email
        val wrongEmailResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to "nonexistent@example.com", "password" to "anyPassword"))
        }
        assertEquals(HttpStatusCode.NotFound, wrongEmailResponse.status)
        assertEquals("Account not found. Please register first.", wrongEmailResponse.body<BaseResponse<Unit>>().message)

        // Case: Login with wrong password
        val wrongPasswordResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to testEmail, "password" to "WrongPass123!"))
        }
        assertEquals(HttpStatusCode.Unauthorized, wrongPasswordResponse.status)
        assertEquals("Invalid password. Please try again.", wrongPasswordResponse.body<BaseResponse<Unit>>().message)

        // Case: Register with existing email
        val existingEmailResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to "Another Name", "email" to testEmail, "password" to "AnotherPass!"))
        }
        assertEquals(HttpStatusCode.Conflict, existingEmailResponse.status)
        assertEquals("An account with this email already exists.", existingEmailResponse.body<BaseResponse<Unit>>().message)

        // Case: Verify with wrong code
        val wrongCodeResponse = client.post("/auth/verify-otp") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to testEmail, "code" to "000000"))
        }
        assertEquals(HttpStatusCode.BadRequest, wrongCodeResponse.status)
        assertEquals("Invalid or expired verification code.", wrongCodeResponse.body<BaseResponse<Unit>>().message)
    }

    @Test
    fun testProtectedProfile() = testApplication {
        setupApp()
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // Try access /auth/me without token
        val response = client.get("/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
