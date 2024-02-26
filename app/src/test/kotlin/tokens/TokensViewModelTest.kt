@file:OptIn(ExperimentalCoroutinesApi::class)

package tokens

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.ObserveBalances
import xyz.argent.candidateassessment.connectivity.ConnectivityStatus
import xyz.argent.candidateassessment.tokens.ObserveTokens
import xyz.argent.candidateassessment.tokens.Token
import xyz.argent.candidateassessment.tokens.TokensState
import xyz.argent.candidateassessment.tokens.TokensViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TokensViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val coroutineScope = CloseableCoroutineScope(dispatcher)

    private fun viewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        connectivity: Flow<ConnectivityStatus> = flowOf(ConnectivityStatus.Available),
        observeTokens: ObserveTokens = ObserveTokens { flowOf(tokens.take(2)) },
        getBalance: () -> Result<String> = { Result.success("1234") },
        balances: (List<Token>) -> Flow<List<Balance>> = { tokens ->
            flowOf(tokens.map { Balance(it, getBalance()) })
        },
    ) =
        TokensViewModel(
            savedStateHandle = savedStateHandle,
            coroutineScope = coroutineScope,
            observeTokens = observeTokens,
            observeBalances = object : ObserveBalances {
                override fun invoke(tokens: List<Token>) = balances(tokens)
                override suspend fun refresh(tokens: List<Token>) = Unit
            },
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
    fun `search tokens`() = runTest {
        val tokens = tokens.take(1)
        val query = tokens.first().name!!
        val balance = Result.success("1234")

        val viewModel = viewModel(
            observeTokens = { flowOf(tokens) },
            getBalance = { balance },
        )

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)

            val expectedBalances = tokens.map { Balance(it, balance) }.toImmutableList()
            assertEquals(
                TokensState(query, expectedBalances), awaitItem(),
            )
        }
    }

    @Test
    fun `observe balances for searched tokens`() = runTest {
        val tokens = tokens.take(2)
        val query = tokens.first().name!!
        val balances = listOf(Balance(tokens.last(), Result.success("1234")))

        val viewModel = viewModel(
            observeTokens = { flowOf(tokens) },
            balances = { flowOf(balances) },
        )

        viewModel.state.test {
            skipItems(1)

            viewModel.search(query)

            assertEquals(
                TokensState(query, balances.toImmutableList()),
                awaitItem(),
            )
        }
    }

    @Test
    fun `get balances after process death when tokens are fetched`() = runTest {
        val queryTokens = listOf(tokens.first().copy(name = "a"), tokens.first().copy(name = "aa"))
        val nonQueryTokens = listOf(tokens.first().copy(name = "b"))
        val tokens = queryTokens + nonQueryTokens
        val balances = tokens.map { Balance(it, Result.success("1234")) }
        val query = "a"

        val savedStateHandle = SavedStateHandle(mapOf(TokensViewModel.QUERY to query))

        val viewModel = viewModel(
            savedStateHandle = savedStateHandle,
            observeTokens = { flowOf(tokens) },
            balances = { flowOf(balances) },
        )

        viewModel.state.test {
            skipItems(1)

            val expectedBalances = balances.toImmutableList()
            assertEquals(
                TokensState(query, expectedBalances),
                awaitItem(),
            )
        }
    }
}
