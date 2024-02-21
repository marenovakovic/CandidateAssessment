package xyz.argent.candidateassessment

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class RateLimiter(private val capacity: Int, private val perMillis: Long) {
    init {
        if (capacity <= 0) throw Error.IllegalCapacity
        if (perMillis < 0) throw IllegalArgumentException()
    }

    private var requests = 0

    fun request() {
        val queue = Channel<Int>(50)
        ticker(1000)
            .receiveAsFlow()
            .onEach {

            }
        if (++requests > capacity) throw Error.Overflow
    }

    sealed class Error(message: String) : Throwable(message) {
        data object IllegalCapacity : Error("RateLimiter can't have capacity of 0 and less")
        data object Overflow : Error("RateLimiter past it's capacity")
    }
}
