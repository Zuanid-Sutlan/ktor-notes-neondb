package com.dreamcode.data.services

import com.dreamcode.domain.services.EmailService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.serialization.Serializable

class ResendEmailService(config: ApplicationConfig) : EmailService {
    private val apiKey = config.property("email.apiKey").getString()
    private val fromEmail = config.property("email.from").getString()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Serializable
    private data class ResendRequest(
        val from: String,
        val to: List<String>,
        val subject: String,
        val html: String
    )

    override suspend fun sendOtpEmail(to: String, otp: String) {
        val response = client.post("https://api.resend.com/emails") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(ResendRequest(
                from = fromEmail,
                to = listOf(to),
                subject = "Your Verification Code",
                html = "<strong>Your verification code is: $otp</strong><br><br>This code will expire in 10 minutes."
            ))
        }

        if (response.status.value in 200..299) {
            println("Email sent successfully via Resend to $to")
        } else {
            println("Failed to send email via Resend: ${response.status}")
        }
    }
}
