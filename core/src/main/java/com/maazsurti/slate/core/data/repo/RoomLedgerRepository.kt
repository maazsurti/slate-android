package com.maazsurti.slate.core.data.repo

import com.maazsurti.slate.core.data.db.LedgerDao
import com.maazsurti.slate.core.domain.model.CustomerBalance
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.repo.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLedgerRepository(private val dao: LedgerDao) : LedgerRepository {

    override fun observeBalances(): Flow<List<CustomerBalance>> =
        dao.observeBalances().map { rows -> rows.map { it.toDomain() } }

    override fun observeEntries(customerId: Long): Flow<List<LedgerEntry>> =
        dao.observeEntries(customerId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun record(entry: LedgerEntry): Long = dao.insert(entry.toEntity())
}
