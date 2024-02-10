package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.flatMapLatest

sealed interface TokensState {
    data object Initial : TokensState
    data object Loading : TokensState
    data class Tokens(val query: String, val tokens: List<Token>) : TokensState
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
    private val tokens = MutableStateFlow<Result<List<Token>>?>(null)
    private val query = savedStateHandle.getStateFlow(QUERY, "")
    private val isLoading = MutableStateFlow(false)
    private val tokensState =
        combine(tokens, query, isLoading) { tokens, query, isLoading ->
            when {
                isLoading -> TokensState.Loading
                tokens?.isSuccess == true -> TokensState.Tokens(query, tokens.search(query))
                tokens?.isFailure == true -> TokensState.Error
                else -> TokensState.Initial
            }
        }

    private fun Result<List<Token>>.search(query: String) =
        getOrThrow().filter { it.name.orEmpty().contains(query, ignoreCase = true) }

    val state =
        connectivityObserver
            .status
            .flatMapLatest(
                onUnavailable = { flowOf(TokensState.ConnectivityError) },
                onAvailable = { tokensState }
            )
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), TokensState.Initial)

    fun init() {
        isLoading.update { true }
        coroutineScope.launch {
            tokens.update { getTokens() }
            isLoading.update { false }
        }
    }

    fun retry() = init()

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    companion object {
        private const val QUERY = "query"
    }
}
