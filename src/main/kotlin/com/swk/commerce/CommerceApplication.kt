
package com.swk.commerce

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@EnableFeignClients
@EnableScheduling
@SpringBootApplication
class CommerceApplication

fun main(args: Array<String>) {
	runApplication<CommerceApplication>(*args)
}
