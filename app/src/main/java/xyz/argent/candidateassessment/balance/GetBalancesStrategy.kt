package xyz.argent.candidateassessment.balance

data class GetBalancesStrategy(val maxRequests: Int, val perMillis: Long) {
    companion object {
        val MaxRequestsNoDelay = GetBalancesStrategy(Int.MAX_VALUE, 0)
        val FivePerSecond = GetBalancesStrategy(5, 1_000)
    }
}
