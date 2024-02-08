package xyz.argent.candidateassessment.balance

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
