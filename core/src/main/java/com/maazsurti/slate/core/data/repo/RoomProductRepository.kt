package com.maazsurti.slate.core.data.repo

import com.maazsurti.slate.core.data.db.ProductDao
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.repo.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProductRepository(private val dao: ProductDao) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun all(): List<Product> = dao.all().map { it.toDomain() }

    override suspend fun get(id: Long): Product? = dao.get(id)?.toDomain()

    override suspend fun save(product: Product): Long = dao.upsert(product.toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)
}
