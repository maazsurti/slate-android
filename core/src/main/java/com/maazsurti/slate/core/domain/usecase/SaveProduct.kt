package com.maazsurti.slate.core.domain.usecase

import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.repo.ProductRepository

sealed interface SaveProductResult {
    data class Saved(val id: Long) : SaveProductResult
    data object BlankName : SaveProductResult
    data object NegativePrice : SaveProductResult
    data object NegativeStock : SaveProductResult
}

class SaveProduct(private val products: ProductRepository) {
    suspend operator fun invoke(product: Product): SaveProductResult {
        if (product.name.isBlank()) return SaveProductResult.BlankName
        if (product.price.cents < 0) return SaveProductResult.NegativePrice
        if (product.stock < 0) return SaveProductResult.NegativeStock
        return SaveProductResult.Saved(products.save(product.copy(name = product.name.trim())))
    }
}
