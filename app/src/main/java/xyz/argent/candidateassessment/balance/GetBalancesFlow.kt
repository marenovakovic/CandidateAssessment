package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import xyz.argent.candidateassessment.tokens.Token
import kotlin.time.TimeSource

@Suppress("SuspendFunctionOnCoroutineScope")
class GetBalancesFlow @Inject constructor(
    private val getTokenBalance: GetTokenBalance,
    private val strategy: GetBalancesStrategy = GetBalancesStrategy.FivePerSecond,
) : GetBalances {

    private var lastRequestBatchTime: TimeSource.Monotonic.ValueTimeMark? = null

    private val initialDelay: Long
        get() = when {
            lastRequestBatchTime == null -> 0
            lastRequestBatchTime!!.elapsedNow().inWholeMilliseconds > strategy.perMillis -> 0
            else -> strategy.perMillis
        }

    private suspend fun a(tokens: List<Token>) = coroutineScope {
        val ticker = ticker(
            delayMillis = strategy.perMillis,
            initialDelayMillis = initialDelay,
            mode = TickerMode.FIXED_DELAY,
        )

        val a =
            flowOf(tokens.chunked(5))
                .flatMapConcat { chunks ->
                    ticker
                        .receiveAsFlow()
                        .flatMapConcat { chunks.asFlow() }
                }
    }

    override suspend operator fun invoke(tokens: List<Token>) = coroutineScope {
        delay(initialDelay)
        lastRequestBatchTime = TimeSource.Monotonic.markNow()
        flow {
            val chunks = tokens.chunked(strategy.maxRequests)
            chunks
                .foldIndexed<List<Token>, List<Balance>>(emptyList()) { i, acc, chunk ->
                    val balances = getBalances(chunk)
                    lastRequestBatchTime = TimeSource.Monotonic.markNow()
                    if (i != chunks.size - 1) delay(strategy.perMillis)
                    println("emit")
                    emit(acc + balances)
                    acc + balances
                }
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
