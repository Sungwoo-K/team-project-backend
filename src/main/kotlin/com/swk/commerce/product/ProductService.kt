package com.swk.commerce.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.Resource
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


@Service
class ProductService(private val productClient: ProductClient,
    private val redisTemplate: RedisTemplate<String,String>,
    private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()
    private val FILE_PATH = "files/product"

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
        val dirPath:Path = Paths.get(FILE_PATH)
        if(!Files.exists(dirPath)) {
            Files.createDirectories(dirPath)
        }
        val filesList = mutableListOf<String>()

        val result:Product = mapper.readValue(productData)

        runBlocking {
            val imageUuidNames = result.imageUuidName + listOf(result.mainImageUuidName)
            imageUuidNames.forEach {
                launch {
                    val resource: Resource = productClient.getProductImage(it)
                    val uuidFilePath = dirPath.resolve(it)
                    Files.copy(resource.inputStream, uuidFilePath,StandardCopyOption.REPLACE_EXISTING)
                    filesList.add(it)
                }
            }
        }

        println(filesList)

        val findProduct = Products.select{Products.id eq result.id}

        transaction {
            if(findProduct.empty()) {
                val insertedProduct = Products.insert {
                    it[id] = result.id
                    it[productBrand] = result.productBrand
                    it[productName] = result.productName
                    it[productPrice] = result.productPrice
                    it[category] = result.category
                    it[productDescription] = result.productDescription
                    it[isActive] = result.isActive
                    it[maximumPurchaseQuantity] = result.maximumPurchaseQuantity
                    it[discountRate] = result.discountRate
                    it[mainImageUuidName] = result.mainImageUuidName
                }.resultedValues!!.first()

                ProductImages.batchInsert(filesList) {
                    this[ProductImages.productId] = insertedProduct[Products.id]
                    this[ProductImages.uuidFileName] = it
                }
            } else {
                Products.update({Products.id eq result.id}){
                    it[id] = result.id
                    it[productBrand] = result.productBrand
                    it[productName] = result.productName
                    it[productPrice] = result.productPrice
                    it[category] = result.category
                    it[productDescription] = result.productDescription
                    it[isActive] = result.isActive
                    it[maximumPurchaseQuantity] = result.maximumPurchaseQuantity
                    it[discountRate] = result.discountRate
                }
            }

        }

    }

    @RabbitListener(queues = ["product-payment-result"])
    fun handlePaymentResultData(productData : String) {
        val result:PaymentResult = mapper.readValue(productData)
        println(result)
        transaction {
            Orders.update({ Orders.id eq result.orderId }) {
                it[isPermission] = result.isPermission
            }
        }
    }

    fun sendProductPayment(paymentRequest: PaymentRequest){
        val mappingPaymentRequest = mapper.writeValueAsString(paymentRequest)
        rabbitTemplate.convertAndSend("product-payment", mappingPaymentRequest)
    }

}