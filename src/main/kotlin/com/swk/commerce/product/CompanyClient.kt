package com.swk.commerce.product

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "companyClient")
interface CompanyClient {

    @GetMapping("/api/user/files/{dynamicPart}")
    fun getCompanyImage(@PathVariable dynamicPart:String) : Resource

}