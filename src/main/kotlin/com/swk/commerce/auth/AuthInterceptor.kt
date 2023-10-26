package com.swk.commerce.auth

import com.swk.commerce.auth.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.messaging.handler.HandlerMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.reflect.Method

@Component
class AuthInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (handler is HandlerMethod) {
            val handlerMethod: HandlerMethod = handler
            val method: Method = handlerMethod.method
            println(method)
            if(method.getAnnotation(Auth::class.java) == null) {
                return true
            }

            val token: String = request.getHeader("Authorization")
            println(token)
            if (token.isEmpty()) {
                response.status = 401
                return false
            }

            val profile: AuthProfile? = JwtUtil.validateToken(token.replace("Bearer ", ""))
            if (profile == null) {
                response.status = 401
                return false
            }

            println(profile)
            request.setAttribute("authProfile", profile)
            return true
        }
        return true
    }
}