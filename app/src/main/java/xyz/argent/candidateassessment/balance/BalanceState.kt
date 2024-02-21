package xyz.argent.candidateassessment.balance

sealed interface BalancesState {
    data object Initial : BalancesState
    data object Loading : BalancesState
    data class Success(val balances: List<Balance>) : BalancesState
}
