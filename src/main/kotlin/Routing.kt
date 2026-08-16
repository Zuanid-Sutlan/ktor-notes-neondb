package com.dreamcode

import com.dreamcode.data.repositories.PostgresNoteRepository
import com.dreamcode.data.repositories.PostgresOtpRepository
import com.dreamcode.data.repositories.PostgresUserRepository
import com.dreamcode.data.services.ResendEmailService
import com.dreamcode.presentation.routes.authRoutes
import com.dreamcode.presentation.routes.noteRoutes
import com.dreamcode.presentation.routes.testRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = PostgresUserRepository()
    val otpRepository = PostgresOtpRepository()
    val noteRepository = PostgresNoteRepository()
    val emailService = ResendEmailService(environment.config)

    routing {
        testRoutes()
        authRoutes(userRepository, otpRepository, emailService)
        noteRoutes(noteRepository)
        get("/") {
            call.respondText("Hello, World!")
        }
    }
}
