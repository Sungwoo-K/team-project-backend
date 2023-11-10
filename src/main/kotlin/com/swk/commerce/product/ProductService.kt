package com.swk.commerce.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.Resource
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


@Service
class ProductService(private val productClient: ProductClient,
    private val companyClient: CompanyClient,
    private val redisTemplate: RedisTemplate<String,String>) {
    private val mapper = jacksonObjectMapper()
    private val FILE_PATH = "files/product"

    @Scheduled(fixedRate = 1000 * 60 * 60)
    fun scheduledFetchTopFavoriteProduct() {
        val result = productClient.getTopFavoriteProduct();

        var topTent:List<Long> = listOf()
        var topTable:List<Long> = listOf()
        var topTableware:List<Long> = listOf()
        var topAccessory:List<Long> = listOf()
        var topOther:List<Long> = listOf()

        for(product in result) {
            if(product.category == "tent") {
                topTent = product.ids;
            }
            if(product.category == "table" ) {
                topTable = product.ids
            }
            if(product.category == "tableware" ) {
                topTableware = product.ids
            }
            if(product.category == "accessory" ) {
                topAccessory = product.ids
            }
            if(product.category == "other") {
                topOther = product.ids
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


    @RabbitListener(queues = ["product-register"])
    fun handleProductData(productData : String) {
        val dirPath:Path = Paths.get(FILE_PATH)
        if(!Files.exists(dirPath)) {
            Files.createDirectories(dirPath)
        }
        val filesList = mutableListOf<String>()

        val result:Product = mapper.readValue(productData)

        if(result.mainImageUuidName !== "") {

            runBlocking {
                val imageUuidNames = result.imageUuidName + listOf(result.mainImageUuidName)
                imageUuidNames.forEach {
                    launch {
                        val resource: Resource = productClient.getProductImage(it)
                        val uuidFilePath = dirPath.resolve(it)
                        Files.copy(resource.inputStream, uuidFilePath, StandardCopyOption.REPLACE_EXISTING)
                        filesList.add(it)
                    }
                }
            }
        }


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
                    it[productName] = result.productName
                    it[productPrice] = result.productPrice
                    it[category] = result.category
                    it[isActive] = result.isActive
                    it[maximumPurchaseQuantity] = result.maximumPurchaseQuantity
                    it[discountRate] = result.discountRate
                }
            }

        }

    }

    @RabbitListener(queues = ["company-register"])
    fun handleCompanyData(companyData : String) {
        val dirPath:Path = Paths.get(FILE_PATH)
        if(!Files.exists(dirPath)) {
            Files.createDirectories(dirPath)
        }

        val result:Company = mapper.readValue(companyData)

        if(result.imageUuidName !== "") {
            val resource: Resource = companyClient.getCompanyImage(result.imageUuidName)
            val uuidFilePath = dirPath.resolve(result.imageUuidName)
            Files.copy(resource.inputStream, uuidFilePath, StandardCopyOption.REPLACE_EXISTING)
        }

        val findCompany = Companys.select{Companys.id eq result.id}

        transaction {
            if(findCompany.empty()) {
                Companys.insert {
                    it[id] = result.id
                    it[name] = result.name
                    it[representativeName] = result.representativeName
                    it[intro] = result.intro
                    it[imageUuidName] = result.imageUuidName
                }
            } else {
                Companys.update({Companys.id eq result.id}){
                    it[id] = result.id
                    it[name] = result.name
                    it[representativeName] = result.representativeName
                    it[intro] = result.intro
                    it[imageUuidName] = result.imageUuidName
                }
            }

        }

    }
}

