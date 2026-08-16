package com.dreamcode.domain.services

interface EmailService {
    suspend fun sendOtpEmail(to: String, otp: String)
}
