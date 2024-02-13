package xyz.argent.candidateassessment.balance

data class GetBalancesRateLimit(val maxRequests: Int, val perMillis: Long) {
    companion object {
        val MaxRequestsNoDelay = GetBalancesRateLimit(Int.MAX_VALUE, 0)
        val FivePerSecond = GetBalancesRateLimit(5, 1_000)
    }
}
