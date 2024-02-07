import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.balance.EtherscanApi
import xyz.argent.candidateassessment.balance.EtherscanApiDelay
import xyz.argent.candidateassessment.balance.EtherscanApiFail
import xyz.argent.candidateassessment.balance.EtherscanApiMock
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.tokens.EthExplorerApi
import xyz.argent.candidateassessment.tokens.toToken
import xyz.argent.candidateassessment.tokens.topTokensJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val tenTokens =
    Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(EthExplorerApi.TopTokensResponse::class.java).fromJson(topTokensJson)!!
        .tokens
        .take(10)
        .map(EthExplorerApi.TokenResponse::toToken)

@OptIn(ExperimentalTime::class)
class GetBalancesTest {

    private fun getBalances(api: EtherscanApi = EtherscanApiMock) = GetBalances(api)

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
    fun `get balance from the api`() = runTest {
        val api = EtherscanApiMock
        val spy = spyk(api)
        val getBalances = getBalances(spy)
        val token = tenTokens.first()

        val balances = getBalances(listOf(token))

        assertEquals(token, balances.single().token)
        assertEquals(
            api.getTokenBalance(token.address, "", "").result.toDouble(),
            balances.single().balance.getOrThrow(),
        )

        coVerify(exactly = 1) {
            spy.getTokenBalance(token.address, any(), any())
        }
    }

    @Test
    fun `call api with Constants wallet address and apiKey`() = runTest {
        val api = spyk(EtherscanApiMock)
        val getBalances = getBalances(api)
        val token = tenTokens.first()

        getBalances(listOf(token))

        coVerify(exactly = 1) {
            api.getTokenBalance(any(), Constants.walletAddress, Constants.etherscanApiKey)
        }
    }

    @Test
    fun `error getting balance for single`() = runTest {
        val token = tenTokens.first()
        val getBalances = getBalances(EtherscanApiFail)

        val balances = getBalances(listOf(token))

        assertEquals(1, balances.size)
        assertTrue(balances.single().balance.isFailure)
    }

    @Test
    fun `error getting balance for all tokens`() = runTest {
        val tokens = tenTokens
        val getBalances = getBalances(EtherscanApiFail)

        val balances = getBalances(tokens)

        assertEquals(tokens.size, balances.size)
        assertTrue(balances.map { it.balance }.all { it.isFailure })
    }

    @Test
    fun `get 5 balances for tokens in parallel`() = runTest {
        val delayMillis = 1_000L
        val getBalances = getBalances(EtherscanApiDelay(delayMillis))

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
}
