package com.dreamcode.domain.repositories

import com.dreamcode.domain.models.User

interface UserRepository {
    suspend fun getUserByEmail(email: String): User?
    suspend fun createUser(name: String, email: String, passwordHash: String): User?
    suspend fun updateLastLogin(email: String)
    suspend fun setVerified(email: String)
    suspend fun requestDeletion(email: String)
    suspend fun restoreAccount(email: String)
    suspend fun updatePassword(email: String, newPasswordHash: String)
}

interface OtpRepository {
    suspend fun saveOtp(email: String, code: String, expiresAt: Long)
    suspend fun getOtp(email: String, code: String): Boolean
    suspend fun deleteOtp(email: String)
}
