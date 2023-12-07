package com.swk.commerce.configuration


import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class WebMvcConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                "http://localhost:5000",
                "http://192.168.0.30:5000",
                "http://192.168.100.109:5000",
                "http://192.168.100.159:5000",
                "https://d3a0qva4kjxuze.cloudfront.net",
                "ec2-43-200-11-107.ap-northeast-2.compute.amazonaws.com"
            )
            .allowedMethods("*")
    }

}