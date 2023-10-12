package com.swk.commerce.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/product")
class ProductController(private val productService: ProductService) {

    @GetMapping("/top-favorite")
    fun getTopFavoriteProducts(@RequestParam category : String): List<TopFavoriteProduct> {
        return productService.getTopFavoriteProduct(category)
    }
}