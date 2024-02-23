@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.BalancesState
import xyz.argent.candidateassessment.balance.ObserveBalances
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver

sealed interface TokensState {
    data object Initial : TokensState
    data object Loading : TokensState
    data class Tokens(
        val query: String,
        val balances: ImmutableList<Balance>,
    ) : TokensState

    data object Error : TokensState
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val coroutineScope: CloseableCoroutineScope,
    connectivityObserver: ConnectivityObserver,
    observeTokens: ObserveTokens,
    observeBalances: ObserveBalances,
) : ViewModel(coroutineScope) {

    // I don't like creation side effects and side effects in general.
    // I like to have a trigger.
    // I don't want everything to start when class is created.
    private val start = Channel<Unit>()

    private val query = savedStateHandle.getStateFlow(QUERY, "")

    private val loadingTokens = MutableStateFlow(false)

    private val searchedTokens =
        query.flatMapLatest { observeTokens() }

    private val balances =
        searchedTokens
            .onEach(observeBalances::refresh)
            .flatMapLatest(observeBalances)

    private val tokensState =
        combine(
            connectivityObserver.status,
            query,
            loadingTokens,
            observeTokens()
                .onEach { tokens -> loadingTokens.update { tokens.isEmpty() } },
            balances,
        ) { _, query, loadingTokens, tokens, balances ->
            if (loadingTokens) TokensState.Loading
            else TokensState.Tokens(query, balances.toImmutableList())
        }

    val state =
        start
            .receiveAsFlow()
            .onEach { loadingTokens.update { true } }
            .flatMapLatest { tokensState }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), TokensState.Initial)

    fun init() = start.trySend(Unit)

    fun retry() = init()

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    private fun List<Token>.search(query: String) =
        filter { it.name.orEmpty().contains(query, ignoreCase = true) }

    companion object {
        const val QUERY = "query"
    }
}
