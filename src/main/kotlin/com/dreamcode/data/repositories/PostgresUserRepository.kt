package com.dreamcode.data.repositories

import com.dreamcode.data.database.DatabaseFactory.dbQuery
import com.dreamcode.data.database.OtpTable
import com.dreamcode.data.database.UserTable
import com.dreamcode.domain.models.User
import com.dreamcode.domain.repositories.OtpRepository
import com.dreamcode.domain.repositories.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PostgresUserRepository : UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UserTable.id].value,
        name = row[UserTable.name],
        email = row[UserTable.email],
        passwordHash = row[UserTable.passwordHash],
        verified = row[UserTable.verified],
        createdAt = row[UserTable.createdAt],
        lastLoginAt = row[UserTable.lastLoginAt],
        deletionRequestedAt = row[UserTable.deletionRequestedAt]
    )

    override suspend fun getUserByEmail(email: String): User? = dbQuery {
        UserTable.selectAll().where { UserTable.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun createUser(name: String, email: String, passwordHash: String): User? = dbQuery {
        val insertStatement = UserTable.insert {
            it[UserTable.name] = name
            it[UserTable.email] = email
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.createdAt] = System.currentTimeMillis()
            it[UserTable.verified] = false
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    override suspend fun updateLastLogin(email: String) {
        dbQuery {
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.lastLoginAt] = System.currentTimeMillis()
            }
        }
    }

    override suspend fun setVerified(email: String) {
        dbQuery {
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.verified] = true
            }
        }
    }

    override suspend fun requestDeletion(email: String) {
        dbQuery {
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.deletionRequestedAt] = System.currentTimeMillis()
            }
        }
    }

    override suspend fun restoreAccount(email: String) {
        dbQuery {
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.deletionRequestedAt] = null
            }
        }
    }

    override suspend fun updatePassword(email: String, newPasswordHash: String) {
        dbQuery {
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.passwordHash] = newPasswordHash
            }
        }
    }
}

class PostgresOtpRepository : OtpRepository {
    override suspend fun saveOtp(email: String, code: String, expiresAt: Long) {
        dbQuery {
            OtpTable.deleteWhere { OtpTable.email eq email }
            OtpTable.insert {
                it[OtpTable.email] = email
                it[OtpTable.code] = code
                it[OtpTable.expiresAt] = expiresAt
            }
        }
    }

    override suspend fun getOtp(email: String, code: String): Boolean = dbQuery {
        OtpTable.selectAll().where { (OtpTable.email eq email) and (OtpTable.code eq code) }
            .any { it[OtpTable.expiresAt] > System.currentTimeMillis() }
    }

    override suspend fun deleteOtp(email: String) {
        dbQuery {
            OtpTable.deleteWhere { OtpTable.email eq email }
        }
    }
}
