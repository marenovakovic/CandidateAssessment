package xyz.argent.candidateassessment.tokens

import kotlinx.coroutines.flow.Flow

interface TokensDao {
    val tokens: Flow<List<Token>>

    suspend fun saveTokens(tokens: List<Token>)
}
