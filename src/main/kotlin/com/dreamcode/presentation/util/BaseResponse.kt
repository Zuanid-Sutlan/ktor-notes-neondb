package com.dreamcode.presentation.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T? = null
)

suspend inline fun <reified T> ApplicationCall.respondSuccess(
    message: String = "Success",
    data: T? = null,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    this.respond(status, BaseResponse(status = true, message = message, data = data))
}

suspend fun ApplicationCall.respondError(
    message: String,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    this.respond(status, BaseResponse<Unit>(status = false, message = message))
}
