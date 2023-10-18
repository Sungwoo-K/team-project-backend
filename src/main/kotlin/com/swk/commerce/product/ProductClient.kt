package com.swk.commerce.product

import org.springframework.core.io.Resource
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable



@FeignClient(name = "productClient", url = "http://192.168.100.151:8080")
interface ProductClient {

    @GetMapping("/alsdkmfalskdf")
    fun getTopFavoriteProduct() : List<TopFavoriteProduct>

    @GetMapping("/product/files/{dynamicPart}")
    fun getProductImage(@PathVariable dynamicPart:String) : Resource
}
