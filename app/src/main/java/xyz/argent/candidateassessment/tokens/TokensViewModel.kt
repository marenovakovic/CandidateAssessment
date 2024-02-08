@file:OptIn(ExperimentalCoroutinesApi::class)

package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    data class Tokens(val tokens: List<Token>) : TokensState
    data object ConnectivityError : TokensState
    data object Error : TokensState
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    private val coroutineScope: CloseableCoroutineScope,
    connectivityObserver: ConnectivityObserver,
    private val getTokens: GetTokens,
) : ViewModel(coroutineScope) {
    private val tokens = MutableStateFlow<Result<List<Token>>?>(null)
    private val isLoading = MutableStateFlow(false)
    private val tokensState =
        combine(tokens, isLoading) { tokens, isLoading ->
            when {
                isLoading -> TokensState.Loading
                tokens?.isSuccess == true -> TokensState.Tokens(tokens.getOrThrow())
                tokens?.isFailure == true -> TokensState.Error
                else -> TokensState.Initial
            }
        }
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

    fun search(query: String?) {}
}
