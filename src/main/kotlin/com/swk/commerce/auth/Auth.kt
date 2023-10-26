package com.swk.commerce.auth

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION,AnnotationTarget.PROPERTY_SETTER)
annotation class Auth(
    val require : Boolean = true
)