package xyz.argent.candidateassessment.balance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class BalanceState(val query: String, val balances: List<Balance>) {
    companion object {
        val Initial = BalanceState("", emptyList())
    }
}

class BalanceViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = savedStateHandle.getStateFlow(QUERY, "")

    val state =
        query
            .map { BalanceState(it, emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BalanceState.Initial)

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    companion object {
        private const val QUERY = "query"
    }
}
