package com.maazsurti.slate.core.data.repo

import com.maazsurti.slate.core.data.db.CustomerDao
import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCustomerRepository(private val dao: CustomerDao) : CustomerRepository {

    override fun observeAll(): Flow<List<Customer>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(id: Long): Customer? = dao.get(id)?.toDomain()

    override suspend fun save(customer: Customer): Long = dao.upsert(customer.toEntity())
}
