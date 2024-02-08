@file:OptIn(FlowPreview::class)

package xyz.argent.candidateassessment.balance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.tokens.GetTokens

@HiltViewModel
class BalanceViewModel @Inject constructor(
    coroutineScope: CloseableCoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val getBalances: GetBalances,
    private val getTokens: GetTokens,
) : ViewModel(coroutineScope) {
    private val query = savedStateHandle.getStateFlow(QUERY, "")
    private val isLoading = MutableStateFlow(false)
    private val balances: Flow<Balances> =
        query
            .onEach { if (it.isNotBlank()) isLoading.update { true } }
            .debounce(500)
            .map { query ->
                if (query.isNotBlank())
                    getTokens()
                        .filter { it.name.orEmpty().contains(query, ignoreCase = true) }
                else emptyList()
            }
            .map(getBalances::invoke)
            .onEach { isLoading.update { false } }
            .map(Balances::Success)
            .onStart<Balances> { emit(Balances.Initial) }

    val state = combine(query, balances, isLoading) { query, balances, isLoading ->
        BalanceState(
            query = query,
            balances = when {
                isLoading -> Balances.Loading
                query.isBlank() -> Balances.Initial
                else -> balances
            },
        )
    }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), BalanceState.Initial)

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    companion object {
        private const val QUERY = "query"
    }
}
