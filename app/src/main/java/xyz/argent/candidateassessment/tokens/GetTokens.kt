package xyz.argent.candidateassessment.tokens

import javax.inject.Inject
import xyz.argent.candidateassessment.tokens.persistence.TokensDao
import xyz.argent.candidateassessment.tokens.persistence.toToken
import xyz.argent.candidateassessment.tokens.persistence.toTokenEntity

fun interface GetTokens : suspend () -> Result<List<Token>>

class GetTokensImpl @Inject constructor(
    private val api: EthExplorerApi,
    private val tokensDao: TokensDao,
) : GetTokens {
    override suspend operator fun invoke(): Result<List<Token>> = runCatching {
        tokensDao
            .getAllTokens()
            .takeIf { it.isNotEmpty() }
            ?.map { it.toToken() }
            ?: fetchTokens()
                .also { tokens ->
                    tokensDao.saveTokens(tokens.map { it.toTokenEntity() })
                }
    }

    private suspend fun fetchTokens() =
        api
            .getTopTokens()
            .tokens
            .map(EthExplorerApi.TokenResponse::toToken)
}
