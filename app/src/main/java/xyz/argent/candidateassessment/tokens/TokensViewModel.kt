@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package xyz.argent.candidateassessment.tokens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.balance.BalancesState
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.balance.ObserveBalances
import xyz.argent.candidateassessment.balance.RefreshBalances
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver

sealed interface TokensState {
    data object Initial : TokensState
    data object Loading : TokensState
    data class Tokens(
        val query: String,
        val balancesState: BalancesState,
    ) : TokensState

    data object Error : TokensState
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val coroutineScope: CloseableCoroutineScope,
    connectivityObserver: ConnectivityObserver,
    private val getTokens: GetTokens,
    getBalances: GetBalances,
    observeTokens: ObserveTokens,
    observeBalances: ObserveBalances,
) : ViewModel(coroutineScope) {

    private val query = savedStateHandle.getStateFlow<String?>(QUERY, null)

    private val balances =
        observeTokens()
            .flatMapLatest { tokens ->
                query
                    .filterNotNull()
                    .debounce(500)
                    .mapLatest { query -> tokens.search(query) }
            }
            .onEach(observeBalances::refresh)
            .flatMapLatest(observeBalances)

    val state = combine(query, balances) { query, balances ->
        TokensState.Tokens(query.orEmpty(), BalancesState.Success(balances.toImmutableList()))
    }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), TokensState.Initial)

    fun init() {

    }

//    private suspend fun loadTokens() =
//        getTokens()
//            .fold(
//                onFailure = { TokensState.Error },
//                onSuccess = { TokensState.Tokens("", BalancesState.Initial) },
//            )

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
