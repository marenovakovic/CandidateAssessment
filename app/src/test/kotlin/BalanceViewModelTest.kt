import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.balance.BalanceViewModel
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.balance.EtherscanApiMock
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.tokens.GetTokens
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceViewModelTest {
    private val testCoroutineScope = CloseableCoroutineScope(UnconfinedTestDispatcher())
    private val getBalances = GetBalances(EtherscanApiMock)
    private val getTokens = GetTokens()
    private fun viewModel() =
        BalanceViewModel(testCoroutineScope, SavedStateHandle(), getBalances, getTokens)

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

            assertEquals(BalanceState(query, Balances.Loading), awaitItem())
            assertEquals(BalanceState(query, Balances.Success(emptyList())), awaitItem())
        }

        assertEquals(query, viewModel.state.value.query)
    }
}
