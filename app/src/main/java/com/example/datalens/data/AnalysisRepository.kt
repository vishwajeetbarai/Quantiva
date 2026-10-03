package com.example.datalens.data

import kotlinx.coroutines.flow.Flow

class AnalysisRepository(private val dao: AnalysisDao) {

    val allAnalyses: Flow<List<AnalysisEntity>> = dao.getAllAnalyses()

    suspend fun insert(analysis: AnalysisEntity) {
        dao.insertAnalysis(analysis)
    }

    suspend fun deleteById(id: String) {
        dao.deleteAnalysisById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
