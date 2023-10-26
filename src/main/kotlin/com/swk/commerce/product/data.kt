package com.swk.commerce.product

data class TopFavoriteProduct(
    val id:Long,
    val name: String,
    val category:String,
    val image: String
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
    val imageUuidName: List<String>
)

data class Payment(
    val productId: Long,
    val quantity: Int,
    val address: String
)

data class PaymentRequest(
    val orderId: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val address:String
)

data class PaymentResult(
    val orderId: Long,
    val isPermission: String
)