package com.dreamcode.data.util

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
    fun check(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}
