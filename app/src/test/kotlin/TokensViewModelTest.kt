import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.tokens.TokensState
import xyz.argent.candidateassessment.tokens.TokensViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class TokensViewModelTest {

    private fun viewModel() = TokensViewModel()

    @Test
    fun `initial state`() {
        assertEquals(TokensState.Initial, viewModel().state.value)
    }

    @Test
    fun `get tokens when init is called`() = runTest {
        val viewModel = viewModel()

        viewModel.init()

        viewModel.state.test {
            assertEquals(TokensState.Loading, awaitItem())
        }
    }
}
