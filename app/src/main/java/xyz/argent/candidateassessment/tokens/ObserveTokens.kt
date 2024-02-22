package xyz.argent.candidateassessment.tokens

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import xyz.argent.candidateassessment.tokens.persistence.TokenEntity
import xyz.argent.candidateassessment.tokens.persistence.TokensDao
import xyz.argent.candidateassessment.tokens.persistence.toToken
import xyz.argent.candidateassessment.tokens.persistence.toTokenEntity

fun interface ObserveTokens : () -> Flow<List<Token>>

class ObserveTokensImpl @Inject constructor(
    private val tokensDao: TokensDao,
    private val api: EthExplorerApi,
) : ObserveTokens {

    override fun invoke() =
        tokensDao
            .getAllTokens()
            .onEach {
                if (it.isEmpty()) refreshTokens()
            }
            .map { tokens -> tokens.map(TokenEntity::toToken) }

    private suspend fun refreshTokens() {
        getTokens()
            .getOrNull()
            ?.let { tokens ->
                tokensDao
                    .saveTokens(tokens.map(Token::toTokenEntity))
            }
    }

    private suspend fun getTokens() =
        runCatching {
            api
                .getTopTokens()
                .tokens
                .map(EthExplorerApi.TokenResponse::toToken)
        }
}
