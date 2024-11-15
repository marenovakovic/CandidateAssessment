package xyz.argent.candidateassessment.balance

import kotlinx.collections.immutable.ImmutableList

sealed interface BalancesState {
    data object Initial : BalancesState
    data object Loading : BalancesState
    data class Success(val balances: ImmutableList<Balance>) : BalancesState
}
