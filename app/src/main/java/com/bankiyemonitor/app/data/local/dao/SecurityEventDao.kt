package com.bankiyemonitor.app.data.local.dao

import androidx.room.*
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SecurityEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SecurityEventEntity>): List<Long>

    @Update
    suspend fun update(event: SecurityEventEntity)

    @Delete
    suspend fun delete(event: SecurityEventEntity)

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE id = :id")
    suspend fun getEventById(id: Long): SecurityEventEntity?

    @Query("SELECT * FROM security_events WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getUnresolvedEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE riskLevel IN ('HIGH', 'CRITICAL') AND isResolved = 0 ORDER BY timestamp DESC")
    fun getCriticalEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE eventType = :type ORDER BY timestamp DESC")
    fun getEventsByType(type: String): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEventsByDateRange(startTime: Long, endTime: Long): Flow<List<SecurityEventEntity>>

    @Query("SELECT COUNT(*) FROM security_events WHERE isResolved = 0")
    fun getUnresolvedCount(): Flow<Int>

    @Query("UPDATE security_events SET isResolved = 1, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolveEvent(id: Long, resolvedAt: Long)

    @Query("UPDATE security_events SET userNotes = :notes WHERE id = :id")
    suspend fun addNotes(id: Long, notes: String)
}
