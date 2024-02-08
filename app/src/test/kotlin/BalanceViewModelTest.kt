import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testCoroutineScope = CloseableCoroutineScope(testDispatcher)
    private val getBalances = GetBalances(EtherscanApiMock, GetBalancesStrategy.MaxRequestsNoDelay)
    private val getTokens = GetTokens()
    private fun viewModel() =
        BalanceViewModel(testCoroutineScope, SavedStateHandle(), getBalances, getTokens)

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
            advanceTimeBy(500)

            val tokens = getTokens().filter { it.name.orEmpty().contains(query) }
            val expectedBalances = getBalances(tokens)
            assertEquals(BalanceState(query, Balances.Success(expectedBalances)), awaitItem())
        }

        assertEquals(query, viewModel.state.value.query)
    }
}
