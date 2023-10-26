package com.swk.commerce.auth.util

import at.favre.lib.crypto.bcrypt.BCrypt

object HashUtil {
    fun createHash(cipherText: String): String {
        return BCrypt
            .withDefaults()
            .hashToString(12, cipherText.toCharArray())
    }

    fun verifyHash(ciphertext: String, hash: String): Boolean {
        return BCrypt
            .verifyer()
            .verify(ciphertext.toCharArray(), hash).verified
    }
}