@file:OptIn(ExperimentalCoroutinesApi::class)

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.ConnectivityStatus
import xyz.argent.candidateassessment.tokens.GetTokens
import xyz.argent.candidateassessment.tokens.TokensState
import xyz.argent.candidateassessment.tokens.TokensViewModel
import xyz.argent.candidateassessment.tokens.tokens
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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
            savedStateHandle = SavedStateHandle(),
            coroutineScope = coroutineScope,
            connectivityObserver = object : ConnectivityObserver {
                override val status = connectivity
            },
            getTokens = getTokens,
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state`() {
        assertEquals(TokensState.Initial, viewModel().state.value)
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

    @Ignore
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

    @Test
    fun `get tokens when init is called`() = runTest {
        val tokens = tokens

        val viewModel = viewModel(getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(
                TokensState.Tokens("", tokens, Balances.Initial),
                awaitItem(),
            )
        }
    }

    @Test
    fun `get tokens when init is called and connection is available`() = runTest {
        val connectivity = flowOf(ConnectivityStatus.Unavailable, ConnectivityStatus.Available)
        val tokens = tokens

        val viewModel =
            viewModel(connectivity = connectivity, getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(
                TokensState.Tokens("", tokens, Balances.Initial),
                awaitItem(),
            )
        }
    }

    @Test
    fun `retry on Error`() = runTest {
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
            assertEquals(
                TokensState.Tokens("", tokens, Balances.Initial),
                awaitItem(),
            )
        }
    }

    @Test
    fun `search tokens`() = runTest {
        val queryTokens = listOf(tokens.first().copy(name = "a"), tokens.first().copy(name = "aa"))
        val nonQueryTokens = listOf(tokens.first().copy(name = "b"))
        val tokens = queryTokens + nonQueryTokens
        val query = "a"

        val viewModel = viewModel(getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()
            skipItems(2)

            viewModel.search(query)

            assertEquals(
                TokensState.Tokens(query, queryTokens, Balances.Initial),
                awaitItem(),
            )
        }
    }

    @Test
    fun `search tokens ignoreCase = true`() = runTest {
        val queryToken = tokens.first().copy(name = "A")
        val nonQueryToken = tokens.first().copy(name = "b")
        val tokens = listOf(queryToken, nonQueryToken)
        val query = queryToken.name!!.lowercase()

        val viewModel = viewModel(getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()
            skipItems(2)

            viewModel.search(query)

            assertEquals(
                TokensState.Tokens(query, listOf(queryToken), Balances.Initial),
                awaitItem(),
            )
        }
    }

    @Ignore
    @Test
    fun `get balances for searched tokens`() = runTest {
        val queryToken = tokens.first().copy(name = "A")
        val nonQueryToken = tokens.first().copy(name = "b")
        val tokens = listOf(queryToken, nonQueryToken)
        val query = queryToken.name!!.lowercase()

        val viewModel = viewModel(getTokens = { Result.success(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()
            skipItems(2)

            viewModel.search(query)
            skipItems(1)

            assertEquals(
                TokensState.Tokens(query, listOf(queryToken), Balances.Loading),
                awaitItem(),
            )
        }
    }
}
