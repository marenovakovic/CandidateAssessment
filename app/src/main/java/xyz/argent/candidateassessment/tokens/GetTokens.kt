package xyz.argent.candidateassessment.tokens

import javax.inject.Inject

fun interface GetTokens : suspend () -> Result<List<Token>>

class GetTokensImpl @Inject constructor(private val api: EthExplorerApi) : GetTokens {
    override suspend operator fun invoke() =
        runCatching {
            api
                .getTopTokens()
                .tokens.take(3)
                .map(EthExplorerApi.TokenResponse::toToken)
        }
}
