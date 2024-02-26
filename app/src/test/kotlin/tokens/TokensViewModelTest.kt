@file:OptIn(ExperimentalCoroutinesApi::class)

package tokens

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
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
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
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
        balances: Flow<List<Balance>> =
            flowOf(
                tokens.map {
                    Balance(it, Result.success("1234"))
                },
            ),
    ) =
        TokensViewModel(
            savedStateHandle = savedStateHandle,
            coroutineScope = coroutineScope,
            observeTokens = observeTokens,
            observeBalances = object : ObserveBalances {
                override fun invoke(tokens: List<Token>) = balances
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
    fun `show loading and then load tokens`() = runTest {
        val tokens = tokens.take(2)
        val balances = tokens.map { Balance(it, Result.success("1234")) }

        val viewModel = viewModel(
            observeTokens = { flowOf(emptyList(), tokens) },
            balances = flowOf(balances)
        )

        viewModel.state.test {
            skipItems(1)

            viewModel.init()

            assertEquals(TokensState.Loading, awaitItem())
            assertEquals(
                TokensState.Tokens("", balances.toImmutableList()),
                awaitItem(),
            )
        }
    }

    @Test
    fun `search tokens`() = runTest {
        val tokens = tokens.take(2)
        val query = tokens.first().name!!

        val viewModel = viewModel(observeTokens = { flowOf(tokens) })

        viewModel.state.test {
            skipItems(1)

            viewModel.init()
            skipItems(1)

            viewModel.search(query)

            assertEquals(TokensState.Tokens("", persistentListOf()), awaitItem())
            assertEquals(TokensState.Tokens(query, persistentListOf()), awaitItem())
        }
    }

    @Test
    fun `observe balances for searched tokens`() = runTest {
        val tokens = tokens.take(2)
        val query = tokens.first().name!!
        val balances = listOf(Balance(tokens.last(), Result.success("1234")))

        val viewModel = viewModel(
            observeTokens = { flowOf(tokens) },
            balances = flowOf(balances),
        )

        viewModel.state.test {
            skipItems(1)

            viewModel.init()
            skipItems(1)

            viewModel.search(query)

            assertEquals(
                TokensState.Tokens(query, balances.toImmutableList()),
                awaitItem(),
            )
        }
    }

//    @Test
//    fun `error getting tokens`() = runTest {
//        val viewModel = viewModel(getTokens = { Result.failure(Throwable()) })
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//
//            assertEquals(TokensState.Loading, awaitItem())
//            assertEquals(TokensState.Error, awaitItem())
//        }
//    }
//
////    @Ignore
////    @Test
////    fun `connectivity not available`() = runTest {
////        val connectivity = flowOf(ConnectivityStatus.Unavailable)
////
////        val viewModel = viewModel(connectivity = connectivity)
////
////        viewModel.state.test {
////            skipItems(1)
////
////            viewModel.init()
////
////            assertEquals(TokensState.ConnectivityError, awaitItem())
////        }
////    }
//
//    @Test
//    fun `get tokens when init is called and connection is available`() = runTest {
//        val connectivity = flowOf(ConnectivityStatus.Unavailable, ConnectivityStatus.Available)
//        val tokens = tokens
//
//        val viewModel =
//            viewModel(connectivity = connectivity, getTokens = { Result.success(tokens) })
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//
//            assertEquals(TokensState.Loading, awaitItem())
//            assertEquals(
//                TokensState.Tokens("", BalancesState.Initial),
//                awaitItem(),
//            )
//        }
//    }
//
//    @Test
//    fun `retry on Error`() = runTest {
//        var shouldFail = true
//        val tokens = tokens
//
//        val viewModel = viewModel(
//            getTokens = {
//                if (shouldFail) {
//                    shouldFail = false
//                    Result.failure(Throwable())
//                } else Result.success(tokens)
//            },
//        )
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//
//            assertEquals(TokensState.Loading, awaitItem())
//            assertEquals(TokensState.Error, awaitItem())
//
//            viewModel.retry()
//
//            assertEquals(TokensState.Loading, awaitItem())
//            assertEquals(
//                TokensState.Tokens("", BalancesState.Initial),
//                awaitItem(),
//            )
//        }
//    }
//
//    @Test
//    fun `search tokens`() = runTest {
//        val queryTokens = listOf(tokens.first().copy(name = "a"), tokens.first().copy(name = "aa"))
//        val nonQueryTokens = listOf(tokens.first().copy(name = "b"))
//        val tokens = queryTokens + nonQueryTokens
//        val query = "a"
//
//        val getBalances = GetBalances { tokens ->
//            tokens.map { Balance(it, Result.success("0.0")) }
//        }
//        val viewModel = viewModel(
//            getTokens = { Result.success(tokens) },
//            getBalances = getBalances,
//        )
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//            skipItems(2)
//
//            viewModel.search(query)
//
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Initial),
//                awaitItem(),
//            )
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Loading),
//                awaitItem(),
//            )
//            val expectedBalances = getBalances(queryTokens).toImmutableList()
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Success(expectedBalances)),
//                awaitItem(),
//            )
//        }
//    }
//
//    @Test
//    fun `search tokens ignoreCase = true`() = runTest {
//        val queryToken = tokens.first().copy(name = "A")
//        val nonQueryToken = tokens.first().copy(name = "b")
//        val tokens = listOf(queryToken, nonQueryToken)
//        val query = queryToken.name!!.lowercase()
//
//        val viewModel = viewModel(getTokens = { Result.success(tokens) })
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//            skipItems(2)
//
//            viewModel.search(query)
//
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Initial),
//                awaitItem(),
//            )
//            cancelAndIgnoreRemainingEvents()
//        }
//    }
//
//    @Test
//    fun `search ALL tokens and not already searched ones`() = runTest {
//        val tokenA = tokens.first().copy(name = "a")
//        val tokenB = tokens.first().copy(name = "b")
//        val tokens = listOf(tokenA, tokenB)
//        val getBalances = GetBalances {
//            it.map { token ->
//                Balance(token, Result.success("0.0"))
//            }
//        }
//
//        val viewModel = viewModel(
//            getTokens = { Result.success(tokens) },
//            getBalances = getBalances,
//        )
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//            skipItems(2)
//
//            viewModel.search(tokenA.name!!)
//            viewModel.search(tokenB.name!!)
//
//            assertEquals(
//                TokensState.Tokens(tokenA.name!!, BalancesState.Initial),
//                awaitItem(),
//            )
//            assertEquals(
//                TokensState.Tokens(tokenA.name!!, BalancesState.Loading),
//                awaitItem(),
//            )
//            skipItems(3)
//            assertEquals(
//                TokensState.Tokens(
//                    tokenB.name!!,
//                    BalancesState.Success(getBalances(listOf(tokenB)).toImmutableList()),
//                ),
//                awaitItem(),
//            )
//        }
//    }
//
//    @Test
//    fun `get balances for searched tokens`() = runTest {
//        val queryToken = tokens.first().copy(name = "A")
//        val nonQueryToken = tokens.first().copy(name = "b")
//        val tokens = listOf(queryToken, nonQueryToken)
//        val balances = tokens.map { Balance(it, Result.success("1234")) }
//        val query = queryToken.name!!.lowercase()
//        val getBalances = GetBalances { balances }
//
//        val viewModel = viewModel(
//            getTokens = { Result.success(tokens) },
//            getBalances = getBalances,
//        )
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//            skipItems(2)
//
//            viewModel.search(query)
//
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Initial),
//                awaitItem(),
//            )
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Loading),
//                awaitItem(),
//            )
//            assertEquals(
//                TokensState.Tokens(
//                    query,
//                    BalancesState.Success(getBalances(tokens).toImmutableList()),
//                ),
//                awaitItem(),
//            )
//        }
//    }
//
//    @Test
//    fun `get balances after process death when tokens are fetched`() = runTest {
//        val queryTokens = listOf(tokens.first().copy(name = "a"), tokens.first().copy(name = "aa"))
//        val nonQueryTokens = listOf(tokens.first().copy(name = "b"))
//        val tokens = queryTokens + nonQueryTokens
//        val query = "a"
//
//        val getBalances = GetBalances {
//            it.map { Balance(it, Result.success("0.0")) }
//        }
//        val savedStateHandle = SavedStateHandle(mapOf(TokensViewModel.QUERY to query))
//        val viewModel = viewModel(
//            savedStateHandle = savedStateHandle,
//            getTokens = { Result.success(tokens) },
//            getBalances = getBalances,
//        )
//
//        viewModel.state.test {
//            skipItems(1)
//
//            viewModel.init()
//            skipItems(2)
//
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Loading),
//                awaitItem(),
//            )
//            val expectedBalances = getBalances(queryTokens).toImmutableList()
//            assertEquals(
//                TokensState.Tokens(query, BalancesState.Success(expectedBalances)),
//                awaitItem(),
//            )
//        }
//    }
//
//    @Ignore
//    @Test
//    fun `connectivity available search, not available, available gets balances for query before losing connectivity`() =
//        runTest {
//            val connectivity = MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Available)
//
//            val viewModel = viewModel(connectivity = connectivity)
//
//            viewModel.state.test {
//                skipItems(1)
//
//                viewModel.init()
//                skipItems(2)
//
//                viewModel.search("")
//                skipItems(2)
//
//                val tokensWithBalances = awaitItem()
//
//                connectivity.value = ConnectivityStatus.Unavailable
//                skipItems(1)
//
//                connectivity.value = ConnectivityStatus.Available
//                skipItems(2)
//                assertEquals(tokensWithBalances, awaitItem())
//            }
//        }
}
