package com.swk.commerce.product

data class TopFavoriteProduct(
    val ids:List<Long>,
    val category:String
)

data class Product(
    val id: Long,
    val productBrand: String,
    val productName: String,
    val productPrice: Long,
    val category: String,
    val productDescription: String,
    val isActive: Boolean,
    val maximumPurchaseQuantity: Int,
    val discountRate:Int,
    val mainImageUuidName : String,
    var imageUuidName: List<String>
)

data class Company(
    val id:Long,
    val name:String,
    val representativeName:String,
    val intro:String,
    val imageUuidName:String
)
