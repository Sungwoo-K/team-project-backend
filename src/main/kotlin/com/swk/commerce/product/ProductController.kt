package com.swk.commerce.product

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Paths





@RestController
@RequestMapping("/product")
class ProductController(private val productService: ProductService, private val resourceLoader: ResourceLoader) {
    private val FILE_PATH = "files/product"

    @GetMapping("/payment")
    fun sendPayment(@RequestBody payment: Payment) {

        transaction {
            val result = Orders.insert {
                it[this.userId] = 1
                it[this.isPermission] = "wait"
                it[this.productId] = payment.productId
                it[this.quantity] = payment.quantity
            }.resultedValues!!.first()

            val paymentRequest = PaymentRequest(result[Orders.id],1, payment.productId, payment.quantity, payment.address)

            productService.sendProductPayment(paymentRequest)
        }


    }

    @GetMapping("/top-favorite")
    fun getTopFavoriteProducts(@RequestParam category : String): List<TopFavoriteProduct> {
        return productService.getTopFavoriteProduct(category)
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
}