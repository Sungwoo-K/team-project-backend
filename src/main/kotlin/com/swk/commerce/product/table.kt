package com.swk.commerce.product

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Orders: Table() {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val isPermission = varchar("is_permission", 30)
    val productId = reference("product_id", Products.id)
    override val primaryKey = PrimaryKey(id)
}

object Products: Table() {
    val id = long("id")
    val productBrand = varchar("product_brand", 100)
    val productName = varchar("product_name", 100)
    val productPrice = long("product_price")
    val category = varchar("category", 100)
    val productDescription = text("product_description")
    val isActive = bool("is_active")
    override val primaryKey = PrimaryKey(id)
}
object ProductImages:Table() {
    val productId = reference("product_id", Products.id)
    val uuidFileName = varchar("uuid", 100).uniqueIndex()
}

@Configuration
class PostTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Products,ProductImages,Orders)
        }
    }
}