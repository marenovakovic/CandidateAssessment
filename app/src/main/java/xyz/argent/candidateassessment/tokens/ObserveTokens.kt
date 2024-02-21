package xyz.argent.candidateassessment.tokens

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

fun interface ObserveTokens : () -> Flow<List<Token>>

class ObserveTokensImpl(
    private val tokensDao: TokensDao,
    private val getTokens: GetTokens,
) : ObserveTokens {
    override fun invoke(): Flow<List<Token>> =
        tokensDao
            .tokens
            .onStart {
                getTokens().getOrNull()?.let { tokensDao.saveTokens(it) }
            }
}
