package com.dreamcode.presentation.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val ACCESS_TOKEN_EXPIRATION = 3_600_000 // 1 hour
    private const val REFRESH_TOKEN_EXPIRATION = 2_592_000_000 // 30 days
    
    private var secret = "my-secret"
    private var issuer = "com.dreamcode"
    private var audience = "notes-audience"
    
    fun init(secret: String, issuer: String, audience: String) {
        this.secret = secret
        this.issuer = issuer
        this.audience = audience
    }

    private val algorithm by lazy { Algorithm.HMAC256(secret) }

    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    fun generateAccessToken(userId: Int, email: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .sign(algorithm)

    fun generateRefreshToken(userId: Int, email: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
        .sign(algorithm)
}
