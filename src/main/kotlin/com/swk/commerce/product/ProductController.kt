package com.swk.commerce.product

import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/product")
class ProductController(private val productService: ProductService, private val resourceLoader: ResourceLoader) {
    private val FILE_PATH = "files/product"

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