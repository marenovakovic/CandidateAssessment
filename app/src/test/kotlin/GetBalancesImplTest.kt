import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.balance.GetBalancesImpl
import xyz.argent.candidateassessment.balance.GetBalancesStrategy
import xyz.argent.candidateassessment.balance.GetTokenBalance
import xyz.argent.candidateassessment.tokens.tokens
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val tenTokens = tokens.take(10)

@OptIn(ExperimentalTime::class)
class GetBalancesImplTest {

    private val GetBalancesStrategy.Companion.OnePerTenMilliseconds: GetBalancesStrategy
        get() = GetBalancesStrategy(1, 10)

    private fun getBalances(
        strategy: GetBalancesStrategy = GetBalancesStrategy.FivePerSecond,
        getTokenBalance: GetTokenBalance = GetTokenBalance { Result.success(Random.nextDouble()) },
    ): GetBalances = GetBalancesImpl(getTokenBalance, strategy)

    @Test
    fun `for empty token list return empty balance list`() = runTest {
        val getBalances = getBalances()

        val balances = getBalances(emptyList())

        assertEquals(emptyList(), balances)
    }

    @Test
    fun `for one token return one balance`() = runTest {
        val token = tenTokens.first()
        val getBalances = getBalances()

        val balances = getBalances(listOf(token))

        assertEquals(1, balances.size)
        assertEquals(token, balances.single().token)
    }

    @Test
    fun `error getting balance for single`() = runTest {
        val token = tenTokens.first()
        val getBalances = getBalances { Result.failure(Throwable()) }

        val balances = getBalances(listOf(token))

        assertEquals(1, balances.size)
        assertTrue(balances.single().balance.isFailure)
    }

    @Test
    fun `error getting balance for all tokens`() = runTest {
        val tokens = tenTokens
        val getBalances = getBalances { Result.failure(Throwable()) }

        val balances = getBalances(tokens)

        assertEquals(tokens.size, balances.size)
        assertTrue(balances.map { it.balance }.all { it.isFailure })
    }

    @Test
    fun `get 5 balances for tokens in parallel`() = runTest {
        val delayMillis = 1_000L
        val getBalances = getBalances {
            delay(delayMillis)
            Result.success(Random.nextDouble())
        }

        launch {
            val duration = testScheduler.timeSource.measureTime {
                getBalances(tenTokens.take(5))
            }

            assertEquals(1, duration.inWholeSeconds)
        }
    }

    @Test
    fun `get maximum 5 tokens per second, slightly more than a second for 10 tokens`() = runTest {
        val tokens = tenTokens
        val getBalances = getBalances()

        launch {
            val duration = testScheduler.timeSource.measureTime {
                getBalances(tokens)
            }

            assertEquals(1, duration.inWholeSeconds)
        }
    }

    @Test
    fun `takes slightly more than a 2 seconds for 15 tokens`() = runTest {
        val tokens = tenTokens + tenTokens.take(5)
        val getBalances = getBalances()

        launch {
            val duration = testScheduler.timeSource.measureTime {
                getBalances(tokens)
            }

            assertEquals(2, duration.inWholeSeconds)
        }
    }

    @Test
    fun `takes slightly more than a 3 seconds for 20 tokens, rounds to 3 seconds`() = runTest {
        val tokens = tenTokens + tenTokens
        val getBalances = getBalances()

        launch {
            val duration = testScheduler.timeSource.measureTime {
                getBalances(tokens)
            }

            assertEquals(3, duration.inWholeSeconds)
        }
    }

    @Test
    fun `strategy rules still apply across invocations`() = runTest {
        val tokens = tenTokens
        val strategy = GetBalancesStrategy.FivePerSecond
        val getBalances = getBalances(strategy = strategy)

        launch {
            val duration = testScheduler.timeSource.measureTime {
                getBalances(tokens)
                getBalances(tokens)
            }

            val delayBetweenInvocations = strategy.perMillis
            assertEquals(
                strategy.perMillis * 2 + delayBetweenInvocations,
                duration.inWholeMilliseconds,
            )
        }
    }
}
