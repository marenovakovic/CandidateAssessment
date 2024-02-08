package xyz.argent.candidateassessment.tokens

import javax.inject.Inject

class GetTokens @Inject constructor(private val api: EthExplorerApi) {
    suspend operator fun invoke() =
        api
            .getTopTokens()
            .tokens
            .map(EthExplorerApi.TokenResponse::toToken)
}
