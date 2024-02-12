package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import xyz.argent.candidateassessment.tokens.Token
import kotlin.time.TimeSource

fun interface GetBalances : suspend (List<Token>) -> List<Balance>

@Suppress("SuspendFunctionOnCoroutineScope")
class GetBalancesImpl @Inject constructor(
    private val getTokenBalance: GetTokenBalance,
    private val strategy: GetBalancesStrategy = GetBalancesStrategy.FivePerSecond,
) : GetBalances {

    private var previousInvokeEnd: TimeSource.Monotonic.ValueTimeMark? = null

    private val initialDelay: Long
        get() = when {
            previousInvokeEnd == null -> 0
            previousInvokeEnd!!.elapsedNow().inWholeMilliseconds > strategy.perMillis -> 0
            else -> strategy.perMillis - previousInvokeEnd!!.elapsedNow().inWholeMilliseconds
        }

    override suspend operator fun invoke(tokens: List<Token>) =
        coroutineScope {
            delay(initialDelay)
            getBalancesWithRateLimit(tokens)
        }

    private suspend fun CoroutineScope.getBalancesWithRateLimit(tokens: List<Token>): List<Balance> {
        val chunks = tokens.chunked(strategy.maxRequests)
        return chunks
            .foldIndexed(emptyList()) { i, acc, chunk ->
                previousInvokeEnd = TimeSource.Monotonic.markNow()
                val balances = getBalances(chunk)
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
