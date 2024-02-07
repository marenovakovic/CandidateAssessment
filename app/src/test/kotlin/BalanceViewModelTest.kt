import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.balance.BalanceViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class BalanceViewModelTest {
    private fun viewModel() = BalanceViewModel(SavedStateHandle())

    @Test
    fun `initial state`() {
        assertEquals(BalanceState("", emptyList()), viewModel().state.value)
    }

    @Test
    fun `search tokens`() = runTest {
        val query = "a"

        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)

            assertEquals(BalanceState(query, emptyList()), awaitItem())
        }

        assertEquals(query, viewModel.state.value.query)
    }
}
