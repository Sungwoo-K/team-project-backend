package com.swk.commerce.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.swk.commerce.auth.AuthProfile
import java.util.*

object JwtUtil {
    var secret = "werojw@eoirjew1313"

    fun validateToken(token: String): AuthProfile? {
        val algorithm = Algorithm.HMAC256(secret)
        val verifier: JWTVerifier = JWT.require(algorithm).build()
        return try {
            val decodedJWT: DecodedJWT = verifier.verify(token)
            val id: Long = java.lang.Long.valueOf(decodedJWT.getSubject())
            val userLoginId: String = decodedJWT
                .getClaim("userLoginId").asString()
            val username: String = decodedJWT
                .getClaim("username").asString()

            AuthProfile(id, userLoginId, username)
        } catch (e: JWTVerificationException) {
            null
        }
    }
}