package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.Balances
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
) : ViewModel(coroutineScope) {

    private val _state = MutableStateFlow<TokensState>(TokensState.Initial)
    val state =
        connectivityObserver
            .status
            .flatMapLatest(
                onUnavailable = { flowOf(TokensState.ConnectivityError) },
                onAvailable = { _state },
            )
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), TokensState.Initial)

    fun init() {
        _state.update { TokensState.Loading }
        coroutineScope.launch {
            _state.update { loadTokens() }
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
        _state.update { currentState ->
            when (currentState) {
                is TokensState.Tokens ->
                    currentState.copy(
                        query = query,
                        tokens = currentState.tokens.search(query),
                    )
                else -> currentState
            }
        }
    }

    private fun List<Token>.search(query: String) =
        filter { it.name.orEmpty().contains(query, ignoreCase = true) }

    companion object {
        private const val QUERY = "query"
    }
}
