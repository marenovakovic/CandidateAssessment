package xyz.argent.candidateassessment.balance

@Suppress("DataClassPrivateConstructor")
data class GetBalancesStrategy private constructor(val maxRequests: Int, val perMillis: Long) {
    companion object {
        val MaxRequestsNoDelay = GetBalancesStrategy(Int.MAX_VALUE, 0)
        val FivePerSecond = GetBalancesStrategy(5, 1_000)
    }
}
