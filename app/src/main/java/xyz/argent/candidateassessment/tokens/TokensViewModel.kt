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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.flatMapLatest

sealed interface TokensState {
    data object Initial : TokensState
    data object Loading : TokensState
    data class Tokens(
        val query: String,
        val tokens: List<Token>,
        val balances: Balances,
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
    private val getBalances: GetBalances,
) : ViewModel(coroutineScope) {

    private val query = savedStateHandle.getStateFlow(QUERY, "")
    private val tokens = MutableStateFlow<TokensState>(TokensState.Initial)

    private val tokensState =
        query.flatMapLatest { query ->
            tokens.mapLatest {
                when (it) {
                    is TokensState.Tokens ->
                        TokensState.Tokens(
                            query = query,
                            tokens = it.tokens.search(query),
                            balances = Balances.Initial,
                        )
                    else -> it
                }
            }
        }

    private val loadingBalances = MutableStateFlow(false)
    private val searchedTokens =
        tokensState
            .filterIsInstance<TokensState.Tokens>()
            .filter { it.query.isNotBlank() }
            .mapLatest { it.tokens }
            .distinctUntilChanged()
    private val balances =
        searchedTokens
            .onEach { loadingBalances.update { true } }
            .mapLatest(getBalances)
            .map(Balances::Success)
            .onEach { loadingBalances.update { false } }
            .onStart<Balances> { emit(Balances.Initial) }

    private val _state =
        combine(tokensState, balances, loadingBalances) { tokensState, balances, loadingBalances ->
            when (tokensState) {
                is TokensState.Tokens -> {
                    tokensState.copy(
                        balances =
                        when {
                            loadingBalances -> Balances.Loading
                            balances is Balances.Success -> balances
                            else -> tokensState.balances
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

    private suspend fun TokensViewModel.loadTokens() =
        getTokens()
            .fold(
                onFailure = { TokensState.Error },
                onSuccess = { TokensState.Tokens("", it, Balances.Initial) },
            )

    fun retry() = init()

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    private fun List<Token>.search(query: String) =
        filter { it.name.orEmpty().contains(query, ignoreCase = true) }

    companion object {
        private const val QUERY = "query"
    }
}
