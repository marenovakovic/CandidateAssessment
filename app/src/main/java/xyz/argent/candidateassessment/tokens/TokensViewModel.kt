package xyz.argent.candidateassessment.tokens

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed interface TokensState {
    data object Initial : TokensState
    data object Loading : TokensState
}

class TokensViewModel {
    val state = MutableStateFlow<TokensState>(TokensState.Initial)

    fun init() {
        state.update { TokensState.Loading }
    }
}
