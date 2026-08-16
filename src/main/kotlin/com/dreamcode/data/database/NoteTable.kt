package com.dreamcode.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object NoteTable : IntIdTable("notes") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val content = text("content")
    val createdAt = long("created_at")
}
