package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import xyz.argent.candidateassessment.tokens.Token
import kotlin.time.TimeSource


@Suppress("SuspendFunctionOnCoroutineScope")
class GetBalancesRec @Inject constructor(
    private val getTokenBalance: GetTokenBalance,
    private val strategy: GetBalancesStrategy = GetBalancesStrategy.FivePerSecond,
) : GetBalances {

    private var lastRequestBatchTime: TimeSource.Monotonic.ValueTimeMark? = null

    private val initialDelay: Long
        get() = when {
            lastRequestBatchTime == null -> 0
            lastRequestBatchTime!!.elapsedNow().inWholeMilliseconds > strategy.perMillis -> 0
            else -> strategy.perMillis - lastRequestBatchTime!!.elapsedNow().inWholeMilliseconds
        }

    override suspend operator fun invoke(tokens: List<Token>) = coroutineScope {
        delay(initialDelay)
        flowOf(getBalancesWithRateLimit(tokens))
    }

    private suspend fun CoroutineScope.getBalancesWithRateLimit(tokens: List<Token>): List<Balance> {
        val chunks = tokens.chunked(strategy.maxRequests)
        return getBalancesRec(chunks)
    }

    private suspend fun CoroutineScope.getBalancesRec(chunks: List<List<Token>>): List<Balance> {
        tailrec suspend fun loop(chunks: List<List<Token>>, acc: List<Balance>): List<Balance> =
            when {
                chunks.isEmpty() -> acc
                chunks.singleOrNull() != null -> acc + getBalances(chunks.single())
                else -> {
                    val balances = getBalances(chunks.last())
                    delay(strategy.perMillis)
                    loop(chunks.dropLast(1), acc + balances)
                }
            }
        return loop(chunks, emptyList())
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
