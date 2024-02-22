package xyz.argent.candidateassessment.tokens

import kotlinx.coroutines.flow.Flow

interface ObserveTokens {
    val tokens: Flow<List<Token>>
    suspend fun refreshTokens()
}

class ObserveTokensImpl(
    private val tokensDao: TokensDao,
    private val getTokens: GetTokens,
) : ObserveTokens {

    override val tokens = tokensDao.tokens

    override suspend fun refreshTokens() {
        getTokens()
            .getOrNull()
            ?.let { tokensDao.saveTokens(it) }
    }
}
