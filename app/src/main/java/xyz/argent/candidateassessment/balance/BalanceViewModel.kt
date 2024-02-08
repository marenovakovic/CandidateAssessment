package xyz.argent.candidateassessment.balance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.tokens.GetTokens

data class BalanceState(val query: String, val balances: Balances) {
    companion object {
        val Initial = BalanceState("", Balances.Initial)
    }
}

sealed interface Balances {
    data object Initial : Balances
    data object Loading : Balances
    data class Success(val balances: List<Balance>) : Balances
}

@HiltViewModel
class BalanceViewModel @Inject constructor(
    coroutineScope: CloseableCoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val getBalances: GetBalances,
    private val getTokens: GetTokens,
) : ViewModel(coroutineScope) {
    private val query = savedStateHandle.getStateFlow(QUERY, "")

    private val balances: Flow<Balances> =
        query
            .filter(String::isNotBlank)
            .debounce(500)
            .map { query -> getTokens().filter { it.name.orEmpty().contains(query) } }
            .map(getBalances::invoke)
            .map(Balances::Success)
            .onStart<Balances> { emit(Balances.Initial) }

    val state = combine(query, balances) { query, balances ->
        BalanceState(query, balances)
    }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), BalanceState.Initial)

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    companion object {
        private const val QUERY = "query"
    }
}
