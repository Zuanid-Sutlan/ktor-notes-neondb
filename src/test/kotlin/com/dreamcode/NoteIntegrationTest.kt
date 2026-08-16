package com.dreamcode

import com.dreamcode.domain.models.AuthData
import com.dreamcode.domain.models.Note
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

class NoteIntegrationTest {

    @Test
    fun testNoteCrudFlow() = testApplication {
        environment {
            config = MapApplicationConfig(
                "storage.driverClassName" to "org.h2.Driver",
                "storage.jdbcURL" to "jdbc:h2:mem:notes_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1",
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

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // 1. Register and get token
        val regResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to "Note User", "email" to "note@example.com", "password" to "Pass123!"))
        }
        val token = regResponse.body<BaseResponse<AuthData>>().data!!.accessToken

        // 2. Create Note
        val createResponse = client.post("/notes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(mapOf("title" to "My First Note", "content" to "Hello World"))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdNote = createResponse.body<BaseResponse<Note>>().data!!
        assertEquals("My First Note", createdNote.title)

        // 3. Get Notes (Paginated)
        val getResponse = client.get("/notes?page=1&pageSize=5") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val notes = getResponse.body<BaseResponse<List<Note>>>().data!!
        assertTrue(notes.isNotEmpty())
        assertEquals("My First Note", notes[0].title)

        // 4. Update Note
        val updateResponse = client.put("/notes/${createdNote.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(mapOf("title" to "Updated Title", "content" to "Updated Content"))
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)

        // 5. Verify Update
        val getOneResponse = client.get("/notes/${createdNote.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val updatedNote = getOneResponse.body<BaseResponse<Note>>().data!!
        assertEquals("Updated Title", updatedNote.title)

        // 6. Delete Note
        val deleteResponse = client.delete("/notes/${createdNote.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // 7. Verify Delete
        val verifyDeleteResponse = client.get("/notes/${createdNote.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, verifyDeleteResponse.status)
    }
}
