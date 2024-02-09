package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import xyz.argent.candidateassessment.tokens.Token

fun interface GetBalances : suspend (List<Token>) -> List<Balance>

@Suppress("SuspendFunctionOnCoroutineScope")
class GetBalancesImpl @Inject constructor(
    private val getTokenBalance: GetTokenBalance,
    private val strategy: GetBalancesStrategy = GetBalancesStrategy.FivePerSecond,
) : GetBalances {
    override suspend operator fun invoke(tokens: List<Token>) =
        coroutineScope {
            val chunks = tokens.chunked(strategy.maxRequests)
            chunks
                .foldIndexed(emptyList<Balance>()) { i, acc, tokens ->
                    val balances = getBalances(tokens)
                    if (i != chunks.size - 1) delay(strategy.perMillis)
                    acc + balances
                }
        }

    private suspend fun CoroutineScope.getBalances(tokens: List<Token>) =
        tokens
            .map { getBalance(it) }
            .awaitAll()
            .map { (token, balance) ->
                Balance(token = token, balance = balance)
            }

    private suspend fun CoroutineScope.getBalance(token: Token) =
        async { token to getTokenBalance(token) }
}

// note: I tried Semaphore implementation too but that didn't work out as expected
