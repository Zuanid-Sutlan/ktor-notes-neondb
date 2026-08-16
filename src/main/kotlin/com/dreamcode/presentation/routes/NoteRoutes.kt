package com.dreamcode.presentation.routes

import com.dreamcode.domain.repositories.NoteRepository
import com.dreamcode.presentation.util.respondError
import com.dreamcode.presentation.util.respondSuccess
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class NoteRequest(val title: String, val content: String)

fun Route.noteRoutes(noteRepository: NoteRepository) {
    authenticate("auth-jwt") {
        route("/notes") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()
                
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10
                
                if (page < 1 || pageSize < 1) {
                    return@get call.respondError("Invalid pagination parameters")
                }

                val notes = noteRepository.getAllNotes(userId, page, pageSize)
                call.respondSuccess("Notes retrieved successfully", notes)
            }

            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondError("Invalid ID")

                val note = noteRepository.getNoteById(id, userId)
                if (note != null) {
                    call.respondSuccess("Note retrieved", note)
                } else {
                    call.respondError("Note not found", HttpStatusCode.NotFound)
                }
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()
                val request = call.receive<NoteRequest>()
                
                if (request.title.isBlank() || request.content.isBlank()) {
                    return@post call.respondError("Title and content cannot be empty")
                }

                val note = noteRepository.addNote(userId, request.title, request.content)
                if (note != null) {
                    call.respondSuccess("Note created", note, HttpStatusCode.Created)
                } else {
                    call.respondError("Failed to create note", HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respondError("Invalid ID")
                val request = call.receive<NoteRequest>()

                val updated = noteRepository.updateNote(id, userId, request.title, request.content)
                if (updated) {
                    call.respondSuccess<Unit>("Note updated")
                } else {
                    call.respondError("Note not found or unauthorized", HttpStatusCode.NotFound)
                }
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respondError("Invalid ID")

                val deleted = noteRepository.deleteNote(id, userId)
                if (deleted) {
                    call.respondSuccess<Unit>("Note deleted")
                } else {
                    call.respondError("Note not found or unauthorized", HttpStatusCode.NotFound)
                }
            }
        }
    }
}
