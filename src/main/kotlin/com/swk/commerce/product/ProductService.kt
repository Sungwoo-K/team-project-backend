package com.swk.commerce.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

data class Product(
    val id: Long,
    val productBrand: String,
    val productName: String,
    val productPrice: Long,
    val category: String,
    val productDescription: String,
    val isActive: Boolean,
    val imageByteArrayList: List<String>
)


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

@Configuration
class PostTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Products)
        }
    }
}

@Service
class ProductService(private val productClient: ProductClient,
    private val redisTemplate: RedisTemplate<String,String>) {
    private val mapper = jacksonObjectMapper()

    @Scheduled(fixedRate = 1000 * 60 * 60)
    fun scheduledFetchTopFavoriteProduct() {
        val result = productClient.getTopFavoriteProduct();

        val topTent:MutableList<TopFavoriteProduct> = mutableListOf()
        val topTable:MutableList<TopFavoriteProduct> = mutableListOf()
        val topTableware:MutableList<TopFavoriteProduct> = mutableListOf()
        val topAccessory:MutableList<TopFavoriteProduct> = mutableListOf()
        val topOther:MutableList<TopFavoriteProduct> = mutableListOf()

        println(result)
        for(product in result) {
            if(product.category === "tent") {
                topTent.add(product)
            }
            if(product.category === "table" ) {
                topTable.add(product)
            }
            if(product.category === "tableware" ) {
                topTableware.add(product)
            }
            if(product.category === "accessory" ) {
                topAccessory.add(product)
            }
            if(product.category === "other") {
                topOther.add(product)
            }
        }

        val keyValuesToAdd= mutableMapOf<String,String>()
        keyValuesToAdd["top-tent"] = mapper.writeValueAsString(topTent)
        keyValuesToAdd["top-table"] = mapper.writeValueAsString(topTable)
        keyValuesToAdd["top-tableware"] = mapper.writeValueAsString(topTableware)
        keyValuesToAdd["top-accessory"] = mapper.writeValueAsString(topAccessory)
        keyValuesToAdd["top-other"] = mapper.writeValueAsString(topOther)

        val keysToDelete:List<String> = listOf("top-tent","top-table" ,"top-tableware","top-accessory" ,"top-other")

        redisTemplate.delete(keysToDelete)
        redisTemplate.opsForValue().multiSet(keyValuesToAdd)
    }

    fun getTopFavoriteProduct(findProductByKey:String) : List<TopFavoriteProduct> {
        val result = redisTemplate.opsForValue().get(findProductByKey)
        if(result != null) {
            val list : List<TopFavoriteProduct> = mapper.readValue(result)
            return list
        } else {
            return listOf()
        }
    }


    @RabbitListener(queues = ["product-register"])
    fun handleProductData(productData : String) {
        val result:Product = mapper.readValue(productData)
        println("Findstart")
        val findProduct = Products.select {
            Products.id eq result.id
        }
        println("Findfinish")

        println("insertstart")
        transaction {
            if(findProduct.empty()){
                Products.insert {
                    it[id] = result.id
                    it[productBrand] = result.productBrand
                    it[productName] = result.productName
                    it[productPrice] = result.productPrice
                    it[category] = result.category
                    it[productDescription] = result.productDescription
                    it[isActive] = result.isActive
                }
            } else {
                Products.update({Products.id eq result.id}) {
                    it[productBrand] = result.productBrand
                    it[productName] = result.productName
                    it[productPrice] = result.productPrice
                    it[category] = result.category
                    it[productDescription] = result.productDescription
                    it[isActive] = result.isActive
                }
            }
        }

        println("insertfinish")


    }
}