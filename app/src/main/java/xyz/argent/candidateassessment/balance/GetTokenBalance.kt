package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.tokens.Token

fun interface GetTokenBalance : suspend (Token) -> Result<String>

class GetTokenBalanceImpl @Inject constructor(private val api: EtherscanApi) : GetTokenBalance {
    override suspend fun invoke(token: Token) =
        runCatching {
            api.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
            .map { it.result }
}
