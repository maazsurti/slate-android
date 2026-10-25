package com.maazsurti.slate.core.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Money,
    val stock: Int,
    val lowStockThreshold: Int = 5,
    val category: Category = Category.Other,
) {
    val isLowStock: Boolean get() = stock <= lowStockThreshold
    val isOutOfStock: Boolean get() = stock <= 0
}
