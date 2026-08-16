package com.dreamcode.presentation.routes

import com.dreamcode.data.util.PasswordHasher
import com.dreamcode.domain.models.AuthData
import com.dreamcode.domain.repositories.OtpRepository
import com.dreamcode.domain.repositories.UserRepository
import com.dreamcode.domain.services.EmailService
import com.dreamcode.presentation.auth.JwtConfig
import com.dreamcode.presentation.util.respondError
import com.dreamcode.presentation.util.respondSuccess
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class VerifyOtpRequest(val email: String, val code: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

fun Route.authRoutes(
    userRepository: UserRepository,
    otpRepository: OtpRepository,
    emailService: EmailService
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val existingUser = userRepository.getUserByEmail(request.email)
            if (existingUser != null) {
                return@post call.respondError("An account with this email already exists.", HttpStatusCode.Conflict)
            }

            val passwordHash = PasswordHasher.hash(request.password)
            val user = userRepository.createUser(request.name, request.email, passwordHash)
            if (user == null) {
                return@post call.respondError("Failed to create account. Please try again later.", HttpStatusCode.InternalServerError)
            }

            // Generate and send OTP for verification
            val otpCode = (100000..999999).random().toString()
            val expiresAt = System.currentTimeMillis() + 600_000 // 10 mins
            otpRepository.saveOtp(request.email, otpCode, expiresAt)
            
            try {
                emailService.sendOtpEmail(request.email, otpCode)
            } catch (e: Exception) {
                println("Failed to send email: ${e.message}")
            }

            val accessToken = JwtConfig.generateAccessToken(user.id!!, user.email)
            val refreshToken = JwtConfig.generateRefreshToken(user.id, user.email)

            call.respondSuccess(
                message = "Registration successful. A verification code has been sent to your email.",
                data = AuthData(user.toResponse(), accessToken, refreshToken),
                status = HttpStatusCode.Created
            )
        }

        post("/verify-otp") {
            val request = call.receive<VerifyOtpRequest>()
            val isValid = otpRepository.getOtp(request.email, request.code)
            if (!isValid) {
                return@post call.respondError("Invalid or expired verification code.", HttpStatusCode.BadRequest)
            }

            userRepository.setVerified(request.email)
            otpRepository.deleteOtp(request.email)
            call.respondSuccess<Unit>("Email verified successfully. Your account is now active.")
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = userRepository.getUserByEmail(request.email)
            
            if (user == null) {
                return@post call.respondError("Account not found. Please register first.", HttpStatusCode.NotFound)
            }
            
            if (!PasswordHasher.check(request.password, user.passwordHash)) {
                return@post call.respondError("Invalid password. Please try again.", HttpStatusCode.Unauthorized)
            }

            if (user.deletionRequestedAt != null) {
                return@post call.respondError("Your account is scheduled for deletion. Please restore it to login.", HttpStatusCode.Forbidden)
            }

            userRepository.updateLastLogin(user.email)

            val otpCode = (100000..999999).random().toString()
            val expiresAt = System.currentTimeMillis() + 600_000 // 10 mins
            otpRepository.saveOtp(request.email, otpCode, expiresAt)

            try {
                emailService.sendOtpEmail(request.email, otpCode)
                call.respondSuccess<Unit>("A login verification code has been sent to your email.")
            } catch (e: Exception) {
                call.respondError("Failed to send verification email. Please try again.", HttpStatusCode.InternalServerError)
            }
        }

        post("/login-verify") {
            val request = call.receive<VerifyOtpRequest>()
            val isValid = otpRepository.getOtp(request.email, request.code)
            if (!isValid) {
                return@post call.respondError("Invalid or expired verification code.", HttpStatusCode.BadRequest)
            }

            otpRepository.deleteOtp(request.email)
            
            val user = userRepository.getUserByEmail(request.email)!!
            val accessToken = JwtConfig.generateAccessToken(user.id!!, user.email)
            val refreshToken = JwtConfig.generateRefreshToken(user.id, user.email)

            call.respondSuccess(
                message = "Login successful.",
                data = AuthData(user.toResponse(), accessToken, refreshToken)
            )
        }

        post("/forgot-password") {
            val email = call.receive<Map<String, String>>()["email"] ?: return@post call.respondError("Email is required")
            val user = userRepository.getUserByEmail(email)
            if (user == null) {
                return@post call.respondError("User not found", HttpStatusCode.NotFound)
            }

            val otpCode = (100000..999999).random().toString()
            val expiresAt = System.currentTimeMillis() + 600_000 // 10 mins
            otpRepository.saveOtp(email, otpCode, expiresAt)

            try {
                emailService.sendOtpEmail(email, otpCode)
                call.respondSuccess<Unit>("Password reset code sent to your email.")
            } catch (e: Exception) {
                call.respondError("Failed to send email: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/reset-password") {
            val request = call.receive<ResetPasswordRequest>()
            val isValid = otpRepository.getOtp(request.email, request.code)
            if (!isValid) {
                return@post call.respondError("Invalid or expired OTP", HttpStatusCode.BadRequest)
            }

            val newPasswordHash = PasswordHasher.hash(request.newPassword)
            userRepository.updatePassword(request.email, newPasswordHash)
            otpRepository.deleteOtp(request.email)
            
            call.respondSuccess<Unit>("Password reset successfully.")
        }

        post("/restore") {
            val request = call.receive<LoginRequest>()
            val user = userRepository.getUserByEmail(request.email)
            if (user == null || !PasswordHasher.check(request.password, user.passwordHash)) {
                return@post call.respondError("Invalid email or password", HttpStatusCode.Unauthorized)
            }

            if (user.deletionRequestedAt == null) {
                return@post call.respondError("Account is not scheduled for deletion", HttpStatusCode.BadRequest)
            }

            userRepository.restoreAccount(request.email)
            call.respondSuccess<Unit>("Account restored successfully. You can now login.")
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val user = userRepository.getUserByEmail(email)
                if (user != null) {
                    call.respondSuccess("User profile", user.toResponse())
                } else {
                    call.respondError("User not found", HttpStatusCode.NotFound)
                }
            }

            delete("/account") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                userRepository.requestDeletion(email)
                call.respondSuccess<Unit>("Account deletion requested. You have 30 days to restore it.")
            }
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            try {
                val decodedJWT = JwtConfig.verifier.verify(request.refreshToken)
                val email = decodedJWT.getClaim("email").asString()
                
                val user = userRepository.getUserByEmail(email)
                if (user != null) {
                    val newAccessToken = JwtConfig.generateAccessToken(user.id!!, email)
                    val newRefreshToken = JwtConfig.generateRefreshToken(user.id, email)
                    call.respondSuccess(
                        message = "Tokens refreshed",
                        data = AuthData(user.toResponse(), newAccessToken, newRefreshToken)
                    )
                } else {
                    call.respondError("User not found", HttpStatusCode.Unauthorized)
                }
            } catch (e: Exception) {
                call.respondError("Invalid refresh token", HttpStatusCode.Unauthorized)
            }
        }
    }
}
