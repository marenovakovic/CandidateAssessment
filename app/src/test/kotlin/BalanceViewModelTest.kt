import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.balance.BalanceViewModel
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.balance.EtherscanApiMock
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.balance.GetBalancesStrategy
import xyz.argent.candidateassessment.tokens.GetTokens
import xyz.argent.candidateassessment.tokens.Token
import xyz.argent.candidateassessment.tokens.tokens
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testCoroutineScope = CloseableCoroutineScope(testDispatcher)
    private val getBalances = GetBalances(EtherscanApiMock, GetBalancesStrategy.MaxRequestsNoDelay)
    private val tenTokens = tokens.take(10)
    private fun viewModel(getTokens: () -> List<Token> = { tenTokens }) =
        BalanceViewModel(
            coroutineScope = testCoroutineScope,
            savedStateHandle = SavedStateHandle(),
            getBalances = getBalances,
            getTokens = mockk<GetTokens> {
                every { this@mockk.invoke() } returns getTokens()
            },
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state`() {
        assertEquals(BalanceState("", Balances.Initial), viewModel().state.value)
    }

    @Test
    fun `get balances for searched tokens`() = runTest {
        val query = "a"

        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)

            assertEquals(BalanceState(query, Balances.Initial), awaitItem())
            assertEquals(BalanceState(query, Balances.Loading), awaitItem())
            val tokens = tenTokens.filter { it.name.orEmpty().contains(query) }
            val expectedBalances = getBalances(tokens)
            assertEquals(BalanceState(query, Balances.Success(expectedBalances)), awaitItem())
        }

        assertEquals(query, viewModel.state.value.query)
    }

    @Test
    fun `searching with empty query string shows initial state`() = runTest {
        val query = ""

        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)

            expectNoEvents()
        }

        assertEquals(query, viewModel.state.value.query)
    }

    @Test
    fun `search with non-empty and then with empty query`() = runTest {
        val query = "a"

        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)

            assertEquals(BalanceState(query, Balances.Initial), awaitItem())
            assertEquals(BalanceState(query, Balances.Loading), awaitItem())
            val tokens = tenTokens.filter { it.name.orEmpty().contains(query) }
            val expectedBalances = getBalances(tokens)
            assertEquals(BalanceState(query, Balances.Success(expectedBalances)), awaitItem())

            viewModel.search("")

            assertEquals(BalanceState.Initial, awaitItem())
        }
    }

    @Test
    fun `search tokens ignoreCase = true`() = runTest {
        val token = tenTokens.first().copy(name = "A")
        val tokens = listOf(token)
        val query = token.name!!.lowercase()

        val viewModel = viewModel { listOf(token) }

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)
            skipItems(2)

            val expectedBalances = getBalances(tokens)
            assertEquals(BalanceState(query, Balances.Success(expectedBalances)), awaitItem())
        }
    }
}
