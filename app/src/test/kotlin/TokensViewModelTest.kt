import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.ConnectivityStatus
import xyz.argent.candidateassessment.tokens.GetTokens
import xyz.argent.candidateassessment.tokens.TokensState
import xyz.argent.candidateassessment.tokens.TokensViewModel
import xyz.argent.candidateassessment.tokens.tokens
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class TokensViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val coroutineScope = CloseableCoroutineScope(dispatcher)

    private fun viewModel(
        connectivity: Flow<ConnectivityStatus> = flowOf(ConnectivityStatus.Available),
        getTokens: GetTokens = GetTokens { Result.success(tokens) },
    ) =
        TokensViewModel(
            coroutineScope = coroutineScope,
            connectivityObserver = object : ConnectivityObserver {
                override val status = connectivity
            },
            getTokens = getTokens,
        )

    @Test
    fun `initial state`() {
        assertEquals(TokensState.Initial, viewModel().state.value)
    }

    @Test
    fun `get tokens when init is called`() = runTest {
        val tokens = tokens

        val viewModel = viewModel(getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(TokensState.Tokens(tokens), awaitItem())
        }
    }

    @Test
    fun `error getting tokens`() = runTest {
        val viewModel = viewModel(getTokens = { Result.failure(Throwable()) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(TokensState.Error, awaitItem())
        }
    }

    @Test
    fun retry() = runTest {
        var shouldFail = true
        val tokens = tokens

        val viewModel = viewModel(
            getTokens = {
                if (shouldFail) {
                    shouldFail = false
                    Result.failure(Throwable())
                } else Result.success(tokens)
            },
        )

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(TokensState.Error, awaitItem())

            viewModel.retry()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(TokensState.Tokens(tokens), awaitItem())
        }
    }

    @Test
    fun `connectivity not available`() = runTest {
        val connectivity = flowOf(ConnectivityStatus.Unavailable)

        val viewModel = viewModel(connectivity = connectivity)

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.ConnectivityError, awaitItem())
        }
    }

    @Ignore
    @Test
    fun `search tokens`() = runTest {
        val tokens = tokens
        val query = "a"

        val viewModel = viewModel(getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()
            skipItems(2)

            viewModel.search(query)

            val expectedTokens = tokens.filter { it.name.orEmpty().contains(query) }
            assertEquals(1, 1)
        }
    }
}
