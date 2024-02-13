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
    private val rateLimit: GetBalancesRateLimit = GetBalancesRateLimit.FivePerSecond,
) : GetBalances {

    private var lastInvokeTimeMark: TimeSource.Monotonic.ValueTimeMark? = null
    private val lastInvokeTimeDif: Long
        get() = lastInvokeTimeMark?.elapsedNow()?.inWholeMilliseconds ?: 0

    private var lastRequestBatchTime: TimeSource.Monotonic.ValueTimeMark? = null
    private val lastRequestBatchTimeDif: Long
        get() = lastRequestBatchTime?.elapsedNow()?.inWholeMilliseconds ?: 0

    private val initialDelay: Long
        get() = when {
            lastRequestBatchTime == null -> 0
            lastInvokeTimeMark == null -> 0
            lastRequestBatchTimeDif < rateLimit.perMillis -> rateLimit.perMillis - lastRequestBatchTimeDif
            lastInvokeTimeDif < rateLimit.perMillis -> rateLimit.perMillis - lastInvokeTimeDif
            else -> 0
        }

    override suspend operator fun invoke(tokens: List<Token>) = coroutineScope {
        delay(initialDelay)
        lastInvokeTimeMark = TimeSource.Monotonic.markNow()
        getBalancesWithRateLimit(tokens)
    }

    private suspend fun CoroutineScope.getBalancesWithRateLimit(tokens: List<Token>): List<Balance> {
        val chunks = tokens.chunked(rateLimit.maxRequests)
        return chunks
            .foldIndexed(emptyList()) { i, acc, chunk ->
                val balances = getBalances(chunk)
                lastRequestBatchTime = TimeSource.Monotonic.markNow()
                if (i != chunks.size - 1) delay(rateLimit.perMillis)
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
