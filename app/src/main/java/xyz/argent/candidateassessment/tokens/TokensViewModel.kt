@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.BalancesState
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.flatMapLatest

sealed interface TokensState {
    data object Initial : TokensState
    data object Loading : TokensState
    data class Tokens(
        val query: String,
        val tokens: List<Token>,
        val balancesState: BalancesState,
    ) : TokensState

    data object ConnectivityError : TokensState
    data object Error : TokensState
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val coroutineScope: CloseableCoroutineScope,
    connectivityObserver: ConnectivityObserver,
    private val getTokens: GetTokens,
    getBalances: GetBalances,
) : ViewModel(coroutineScope) {

    private val query = savedStateHandle.getStateFlow(QUERY, "")
    private val tokens = MutableStateFlow<TokensState>(TokensState.Initial)

    private val tokensState =
        combine(query, tokens) { query, tokensState ->
            when (tokensState) {
                is TokensState.Tokens ->
                    TokensState.Tokens(
                        query = query,
                        tokens = tokensState.tokens.search(query),
                        balancesState = BalancesState.Initial,
                    )
                else -> tokensState
            }
        }

    private val loadingBalances = MutableStateFlow(false)
    private val searchedTokens =
        tokensState
            .filterIsInstance<TokensState.Tokens>()
            .filter { it.query.isNotBlank() }
            .mapLatest { it.tokens }
            .distinctUntilChanged()
    private val balancesState =
        searchedTokens
            .onEach { loadingBalances.update { true } }
            .mapLatest(getBalances)
            .map(BalancesState::Success)
            .onEach { loadingBalances.update { false } }
            .onStart<BalancesState> { emit(BalancesState.Initial) }

    private val _state =
        combine(tokensState, balancesState, loadingBalances) { tokensState, balances, loadingBalances ->
            when (tokensState) {
                is TokensState.Tokens -> {
                    tokensState.copy(
                        balancesState =
                        when {
                            loadingBalances -> BalancesState.Loading
                            balances is BalancesState.Success -> balances
                            else -> tokensState.balancesState
                        },
                    )
                }
                else -> tokensState
            }
        }

    val state =
        connectivityObserver
            .status
            .flatMapLatest(
                onUnavailable = { flowOf(TokensState.ConnectivityError) },
                onAvailable = { _state },
            )
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), TokensState.Initial)

    fun init() {
        tokens.update { TokensState.Loading }
        coroutineScope.launch {
            tokens.update { loadTokens() }
        }
    }

    private suspend fun loadTokens() =
        getTokens()
            .fold(
                onFailure = { TokensState.Error },
                onSuccess = { TokensState.Tokens("", it, BalancesState.Initial) },
            )

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
