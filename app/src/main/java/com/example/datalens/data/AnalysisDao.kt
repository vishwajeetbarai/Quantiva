package com.example.datalens.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Query("SELECT * FROM analyses ORDER BY analyzedAt DESC")
    fun getAllAnalyses(): Flow<List<AnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity)

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteAnalysisById(id: String)

    @Query("DELETE FROM analyses")
    suspend fun deleteAll()
}
