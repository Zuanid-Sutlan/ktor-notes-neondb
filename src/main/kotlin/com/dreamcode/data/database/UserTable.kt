package com.dreamcode.data.database

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val verified = bool("verified").default(false)
    val createdAt = long("created_at")
    val lastLoginAt = long("last_login_at").nullable()
    val deletionRequestedAt = long("deletion_requested_at").nullable()
}

object OtpTable : IntIdTable("otps") {
    val email = varchar("email", 255)
    val code = varchar("code", 6)
    val expiresAt = long("expires_at")
}
