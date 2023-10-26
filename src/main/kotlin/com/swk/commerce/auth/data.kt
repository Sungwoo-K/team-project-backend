package com.swk.commerce.auth

data class AuthProfile(
    val id: Long = 0,
    val userLoginId : String,
    val username: String,
)