package com.swk.commerce.product

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "productClient", url = "http://192.168.100.152:5500")
interface CompanyClient {

    @GetMapping("/user/profileImage/{companyId}/{dynamicPart}")
    fun getCompanyImage(@PathVariable companyId:Long,@PathVariable dynamicPart:String) : Resource

}