@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.ObserveBalances

data class TokensState(
    val query: String,
    val balances: ImmutableList<Balance>,
) {
    companion object {
        val Initial = TokensState("", persistentListOf())
    }
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    coroutineScope: CloseableCoroutineScope,
    observeTokens: ObserveTokens,
    observeBalances: ObserveBalances,
) : ViewModel(coroutineScope) {

    private val query = savedStateHandle.getStateFlow<String?>(QUERY, null)

    private val balances =
        observeTokens()
            .flatMapLatest { tokens ->
                query
                    .filterNotNull()
                    .debounce(250)
                    .mapLatest { query -> tokens.search(query) }
            }
            .onEach {
                coroutineScope.launch {
                    observeBalances.refresh(it)
                }
            }
            .flatMapLatest(observeBalances)

    val state = combine(query, balances) { query, balances ->
        TokensState(query.orEmpty(), balances.toImmutableList())
    }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), TokensState.Initial)

    fun search(query: String) {
        savedStateHandle[QUERY] = query
    }

    private fun List<Token>.search(query: String) =
        filter { it.name.orEmpty().contains(query, ignoreCase = true) }

    companion object {
        const val QUERY = "query"
    }
}
