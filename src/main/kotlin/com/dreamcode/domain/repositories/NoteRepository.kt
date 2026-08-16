package com.dreamcode.domain.repositories

import com.dreamcode.domain.models.Note

interface NoteRepository {
    suspend fun getAllNotes(userId: Int, page: Int, pageSize: Int): List<Note>
    suspend fun getNoteById(id: Int, userId: Int): Note?
    suspend fun addNote(userId: Int, title: String, content: String): Note?
    suspend fun updateNote(id: Int, userId: Int, title: String, content: String): Boolean
    suspend fun deleteNote(id: Int, userId: Int): Boolean
}
