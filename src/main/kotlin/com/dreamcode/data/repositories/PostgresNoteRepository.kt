package com.dreamcode.data.repositories

import com.dreamcode.data.database.DatabaseFactory.dbQuery
import com.dreamcode.data.database.NoteTable
import com.dreamcode.domain.models.Note
import com.dreamcode.domain.repositories.NoteRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PostgresNoteRepository : NoteRepository {

    private fun resultRowToNote(row: ResultRow) = Note(
        id = row[NoteTable.id].value,
        userId = row[NoteTable.userId].value,
        title = row[NoteTable.title],
        content = row[NoteTable.content],
        createdAt = row[NoteTable.createdAt]
    )

    override suspend fun getAllNotes(userId: Int, page: Int, pageSize: Int): List<Note> = dbQuery {
        val offset = ((page - 1) * pageSize).toLong()
        NoteTable
            .selectAll()
            .where { NoteTable.userId eq userId }
            .limit(pageSize)
            .offset(offset)
            .orderBy(NoteTable.createdAt to SortOrder.DESC)
            .map(::resultRowToNote)
    }

    override suspend fun getNoteById(id: Int, userId: Int): Note? = dbQuery {
        NoteTable
            .selectAll()
            .where { (NoteTable.id eq id) and (NoteTable.userId eq userId) }
            .map(::resultRowToNote)
            .singleOrNull()
    }

    override suspend fun addNote(userId: Int, title: String, content: String): Note? = dbQuery {
        val insertStatement = NoteTable.insert {
            it[NoteTable.userId] = userId
            it[NoteTable.title] = title
            it[NoteTable.content] = content
            it[NoteTable.createdAt] = System.currentTimeMillis()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToNote)
    }

    override suspend fun updateNote(id: Int, userId: Int, title: String, content: String): Boolean = dbQuery {
        NoteTable.update({ (NoteTable.id eq id) and (NoteTable.userId eq userId) }) {
            it[NoteTable.title] = title
            it[NoteTable.content] = content
        } > 0
    }

    override suspend fun deleteNote(id: Int, userId: Int): Boolean = dbQuery {
        NoteTable.deleteWhere { (NoteTable.id eq id) and (NoteTable.userId eq userId) } > 0
    }
}
