package com.dreamcode.presentation.routes

import com.dreamcode.data.database.DatabaseFactory.dbQuery
import com.dreamcode.data.database.NoteTable
import com.dreamcode.presentation.util.respondError
import com.dreamcode.presentation.util.respondSuccess
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll

fun Route.testRoutes() {
    get("/test-db") {
        try {
            val count = dbQuery {
                NoteTable.selectAll().count()
            }
            call.respondSuccess("Database connection is working!", mapOf("totalNotes" to count))
        } catch (e: Exception) {
            call.respondError("Database connection failed: ${e.message}")
        }
    }
}
