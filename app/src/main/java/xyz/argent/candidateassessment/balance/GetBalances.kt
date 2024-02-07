package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.tokens.Token

data class GetBalancesStrategy(val maxRequests: Int, val perMillis: Long)

@Suppress("SuspendFunctionOnCoroutineScope")
class GetBalances @Inject constructor(
    private val api: EtherscanApi,
    private val strategy: GetBalancesStrategy = GetBalancesStrategy(5, 1_000),
) {
    suspend operator fun invoke(tokens: List<Token>) =
        coroutineScope {
            tokens
                .chunked(strategy.maxRequests)
                .foldIndexed(emptyList<Balance>()) { i, acc, tokens ->
                    val balances = getBalances(tokens)
                    if (i > 0) delay(strategy.perMillis)
                    println("after")
                    acc + balances
                }
        }

    private suspend fun CoroutineScope.getBalances(tokens: List<Token>) =
        tokens
            .map { getBalance(it) }
            .awaitAll()
            .map { (token, balanceResponse) ->
                val balance = balanceResponse.map { it.result.toDouble() }
                Balance(token = token, balance = balance)
            }

    private suspend fun CoroutineScope.getBalance(token: Token) =
        async {
            token to runCatching {
                api.getTokenBalance(
                    token.address,
                    Constants.walletAddress,
                    Constants.etherscanApiKey,
                )
            }
        }
}
