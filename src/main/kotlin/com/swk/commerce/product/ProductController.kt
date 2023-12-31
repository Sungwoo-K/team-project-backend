package com.swk.commerce.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection


@RestController
@RequestMapping("/api/product")
class ProductController(private val resourceLoader: ResourceLoader, private val redisTemplate: RedisTemplate<String,String>) {
    private val FILE_PATH = "files/product"
    private val mapper = jacksonObjectMapper()

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long):ResponseEntity<Product> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ) {
        val result = Products.select(Products.id eq id).map{
            Product(
                id = it[Products.id],
                productBrand = it[Products.productBrand],
                productName = it[Products.productName],
                productPrice = it[Products.productPrice],
                category = it[Products.category],
                productDescription = it[Products.productDescription],
                isActive = it[Products.isActive],
                maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                discountRate = it[Products.discountRate],
                mainImageUuidName = it[Products.mainImageUuidName],
                imageUuidName = emptyList()
            )
        }.singleOrNull() ?: return@transaction ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val images = ProductImages.select(ProductImages.productId eq id).map {
            it[ProductImages.uuidFileName]
        }

        result.imageUuidName = images;

        return@transaction ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping
    fun getPagingProducts(@RequestParam category: String,@RequestParam page:Int,@RequestParam size:Int)
    :ResponseEntity<Page<Product>> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ){


        if(category == "null") {
            val result = Products.selectAll().orderBy(Products.id to SortOrder.DESC)
                    .limit(size, offset = (size * page).toLong()).map {
                    Product(
                        id = it[Products.id],
                        productBrand = it[Products.productBrand],
                        productName = it[Products.productName],
                        productPrice = it[Products.productPrice],
                        category = it[Products.category],
                        productDescription = it[Products.productDescription],
                        isActive = it[Products.isActive],
                        maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                        discountRate = it[Products.discountRate],
                        mainImageUuidName = it[Products.mainImageUuidName],
                        imageUuidName = emptyList()
                    )
                }


            val totalCount = Products.selectAll().count()
            return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount));
        }

        val result = Products.select(Products.category eq category)
            .orderBy(Products.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong()).map {
                Product(
                    id = it[Products.id],
                    productBrand = it[Products.productBrand],
                    productName = it[Products.productName],
                    productPrice = it[Products.productPrice],
                    category = it[Products.category],
                    productDescription = it[Products.productDescription],
                    isActive = it[Products.isActive],
                    maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                    discountRate = it[Products.discountRate],
                    mainImageUuidName = it[Products.mainImageUuidName],
                    imageUuidName = emptyList()
                )
            }

        val totalCount = Products.select(Products.category eq category).count()
        return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount));
    }

    @GetMapping("discount")
    fun getPagingDiscountProducts(@RequestParam category: String,@RequestParam page:Int,@RequestParam size:Int)
    :ResponseEntity<Page<Product>> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ){


        if(category == "null") {
            val result = Products.select(Products.discountRate greater 0)
                .orderBy(Products.id to SortOrder.DESC)
                .limit(size, offset = (size * page).toLong()).map {
                    Product(
                        id = it[Products.id],
                        productBrand = it[Products.productBrand],
                        productName = it[Products.productName],
                        productPrice = it[Products.productPrice],
                        category = it[Products.category],
                        productDescription = it[Products.productDescription],
                        isActive = it[Products.isActive],
                        maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                        discountRate = it[Products.discountRate],
                        mainImageUuidName = it[Products.mainImageUuidName],
                        imageUuidName = emptyList()
                    )
                }

            val totalCount = Products.select(Products.discountRate greater 0).count()
            return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount))
        }

        val result = Products.select{(Products.discountRate greater 0) and (Products.category eq category)}
            .orderBy(Products.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong()).map {
                Product(
                    id = it[Products.id],
                    productBrand = it[Products.productBrand],
                    productName = it[Products.productName],
                    productPrice = it[Products.productPrice],
                    category = it[Products.category],
                    productDescription = it[Products.productDescription],
                    isActive = it[Products.isActive],
                    maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                    discountRate = it[Products.discountRate],
                    mainImageUuidName = it[Products.mainImageUuidName],
                    imageUuidName = emptyList()
                )
            }

        val totalCount = Products.select{(Products.discountRate greater 0) and (Products.category eq category)}.count()
        return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount))
    }


    @GetMapping("search")
    fun getPagingSearchProducts(@RequestParam keyword: String,@RequestParam category: String,@RequestParam page:Int,@RequestParam size:Int)
            :ResponseEntity<Page<Product>> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ){


        if(category == "null") {
            val result = Products.select{(Products.productBrand like "%${keyword}%") or (Products.productName like "%${keyword}%")}
                .orderBy(Products.id to SortOrder.DESC)
                .limit(size, offset = (size * page).toLong()).map {
                Product(
                    id = it[Products.id],
                    productBrand = it[Products.productBrand],
                    productName = it[Products.productName],
                    productPrice = it[Products.productPrice],
                    category = it[Products.category],
                    productDescription = it[Products.productDescription],
                    isActive = it[Products.isActive],
                    maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                    discountRate = it[Products.discountRate],
                    mainImageUuidName = it[Products.mainImageUuidName],
                    imageUuidName = emptyList()
                )
            }

            val totalCount = Products.select{(Products.productBrand like "%${keyword}%") or (Products.productName like "%${keyword}%")}.count()
            return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount))
        }

        val result = Products.select{((Products.productBrand like "%${keyword}%") or (Products.productName like "%${keyword}%")) and (Products.category eq category)}
            .orderBy(Products.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong()).map {
            Product(
                id = it[Products.id],
                productBrand = it[Products.productBrand],
                productName = it[Products.productName],
                productPrice = it[Products.productPrice],
                category = it[Products.category],
                productDescription = it[Products.productDescription],
                isActive = it[Products.isActive],
                maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                discountRate = it[Products.discountRate],
                mainImageUuidName = it[Products.mainImageUuidName],
                imageUuidName = emptyList()
            )
        }

        val totalCount = Products.select{((Products.productBrand like "%${keyword}%") or (Products.productName like "%${keyword}%")) and (Products.category eq category)}.count()
        return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount))
    }


    @GetMapping("brands/items/{brandName}")
    fun getBrandsProduct(@PathVariable brandName:String,@RequestParam page:Int,@RequestParam size:Int)
            :ResponseEntity<Page<Product>> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ){
        val result = Products.select(Products.productBrand eq brandName)
            .orderBy(Products.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong()).map {
            Product(
                id = it[Products.id],
                productBrand = it[Products.productBrand],
                productName = it[Products.productName],
                productPrice = it[Products.productPrice],
                category = it[Products.category],
                productDescription = it[Products.productDescription],
                isActive = it[Products.isActive],
                maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                discountRate = it[Products.discountRate],
                mainImageUuidName = it[Products.mainImageUuidName],
                imageUuidName = emptyList()
            )
        }

        val totalCount = Products.select(Products.productBrand eq brandName).count()
        return@transaction ResponseEntity.status(HttpStatus.OK).body(PageImpl(result, PageRequest.of(page,size),totalCount))
    }
    @GetMapping("/brands/{brandName}")
    fun getCompany(@PathVariable brandName:String): ResponseEntity<Company> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ) {

        val result = Companys.select(Companys.name eq brandName).map {
            Company(
                id = it[Companys.id],
                name = it[Companys.name],
                representativeName = it[Companys.representativeName],
                intro = it[Companys.intro],
                imageUuidName = it[Companys.imageUuidName]
            )
        }.singleOrNull()

        if(result == null) {
            return@transaction ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return@transaction ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/top-favorite")
    fun getTopFavoriteProducts(): ResponseEntity<List<Map<String,List<Product>>>> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
    ) {
        val topFavoriteMaps = mutableListOf<Map<String,List<Product>>>()

        val topFavoriteKeys = redisTemplate.keys("*")

        runBlocking {
            topFavoriteKeys.forEach {
                launch {
                    val value = redisTemplate.opsForValue().get(it)
                    if(value != null){
                        val list : List<Long> = mapper.readValue(value)
                        val result = Products.select( Products.id inList list).map {
                            Product(
                                id = it[Products.id],
                                productBrand = it[Products.productBrand],
                                productName = it[Products.productName],
                                productPrice = it[Products.productPrice],
                                category = it[Products.category],
                                productDescription = it[Products.productDescription],
                                isActive = it[Products.isActive],
                                maximumPurchaseQuantity = it[Products.maximumPurchaseQuantity],
                                discountRate = it[Products.discountRate],
                                mainImageUuidName = it[Products.mainImageUuidName],
                                imageUuidName = emptyList()
                            )
                        }
                        topFavoriteMaps.add(mapOf(it to result))
                    }
                }
            }
        }
        return@transaction ResponseEntity.status(HttpStatus.OK).body(topFavoriteMaps)
    }

    @GetMapping("/files/{uuidFilename}")
    fun getFileImage(@PathVariable uuidFilename:String) : ResponseEntity<Any> {
        val file = Paths.get("$FILE_PATH/$uuidFilename").toFile()
        if (!file.exists()) {
            return ResponseEntity.notFound().build()
        }

        val mimeType = Files.probeContentType(file.toPath())
        val mediaType = MediaType.parseMediaType(mimeType)

        val resource = resourceLoader.getResource("file:$file")
        return ResponseEntity.ok().contentType(mediaType).body(resource)
    }

    @GetMapping("/files/main-image/{id}")
    fun getFileMainImage(@PathVariable id:Long) : ResponseEntity<Any> = transaction(
        Connection.TRANSACTION_READ_UNCOMMITTED,readOnly = true
    ) {
        val result = Products.select(Products.id eq id).map {
            it[Products.mainImageUuidName]
        }.singleOrNull()

        val file = Paths.get("$FILE_PATH/$result").toFile()
        if (!file.exists()) {
            return@transaction ResponseEntity.notFound().build()
        }

        val mimeType = Files.probeContentType(file.toPath())
        val mediaType = MediaType.parseMediaType(mimeType)

        val resource = resourceLoader.getResource("file:$file")
        return@transaction ResponseEntity.ok().contentType(mediaType).body(resource)
    }
}