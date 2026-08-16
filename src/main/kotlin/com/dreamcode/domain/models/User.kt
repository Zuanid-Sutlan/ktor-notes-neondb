package com.dreamcode.domain.models

import kotlinx.serialization.Serializable

data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val passwordHash: String,
    val verified: Boolean = false,
    val createdAt: Long,
    val lastLoginAt: Long? = null,
    val deletionRequestedAt: Long? = null
) {
    fun toResponse() = UserResponse(
        id = id!!,
        name = name,
        email = email,
        verified = verified,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt,
        deletionRequestedAt = deletionRequestedAt
    )
}

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val verified: Boolean,
    val createdAt: Long,
    val lastLoginAt: Long? = null,
    val deletionRequestedAt: Long? = null
) {
    fun toResponse() = UserResponse(
        id = id!!,
        name = name,
        email = email,
        verified = verified,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt,
        deletionRequestedAt = deletionRequestedAt
    )
}

@Serializable
data class AuthData(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)
