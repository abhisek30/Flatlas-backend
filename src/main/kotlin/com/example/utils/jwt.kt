package com.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun generateToken(userId: Int): String {
    return JWT.create()
        .withAudience("ktor-audience")
        .withIssuer("ktor-issuer")
        .withClaim("userId", userId)
        //.withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.HMAC256("secret"))
}