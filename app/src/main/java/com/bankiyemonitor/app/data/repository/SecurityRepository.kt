package com.bankiyemonitor.app.data.repository

import com.bankiyemonitor.app.data.local.dao.SecurityEventDao
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

class SecurityRepository(
    private val securityEventDao: SecurityEventDao
) {
    fun getAllEvents(): Flow<List<SecurityEventEntity>> = securityEventDao.getAllEvents()

    fun getUnresolvedEvents(): Flow<List<SecurityEventEntity>> = securityEventDao.getUnresolvedEvents()

    fun getCriticalEvents(): Flow<List<SecurityEventEntity>> = securityEventDao.getCriticalEvents()

    fun getEventsByType(type: String): Flow<List<SecurityEventEntity>> =
        securityEventDao.getEventsByType(type)

    fun getEventsByDateRange(startTime: Long, endTime: Long): Flow<List<SecurityEventEntity>> =
        securityEventDao.getEventsByDateRange(startTime, endTime)

    fun getUnresolvedCount(): Flow<Int> = securityEventDao.getUnresolvedCount()

    suspend fun saveEvent(event: SecurityEventEntity): Long = securityEventDao.insert(event)

    suspend fun resolveEvent(id: Long) {
        securityEventDao.resolveEvent(id, System.currentTimeMillis())
    }

    suspend fun addNotes(id: Long, notes: String) = securityEventDao.addNotes(id, notes)
}
