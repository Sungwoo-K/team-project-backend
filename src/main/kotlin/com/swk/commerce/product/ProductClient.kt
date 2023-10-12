package com.swk.commerce.product

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

data class TopFavoriteProduct(
    val id:Long,
    val name: String,
    val category:String,
    val image: String
)

@FeignClient(name = "productClient", url = "http://192.168.100.151:8081")
interface ProductClient {

    @GetMapping
    fun getTopFavoriteProduct() : List<TopFavoriteProduct>
}