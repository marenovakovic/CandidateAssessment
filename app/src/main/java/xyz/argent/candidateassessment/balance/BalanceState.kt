package xyz.argent.candidateassessment.balance

sealed interface Balances {
    data object Initial : Balances
    data object Loading : Balances
    data class Success(val balances: List<Balance>) : Balances
}
