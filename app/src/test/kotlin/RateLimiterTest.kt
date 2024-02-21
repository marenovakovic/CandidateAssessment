import xyz.argent.candidateassessment.RateLimiter
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class RateLimiterTest {

    @Test
    fun `throw when creating limiter with capacity of 0`() {
        assertFailsWith<RateLimiter.Error.IllegalCapacity> {
            RateLimiter(0, 1)
        }
    }

    @Test
    fun `throw when creating limiter with capacity of less than 0`() {
        assertFailsWith<RateLimiter.Error.IllegalCapacity> {
            RateLimiter(-1, 1)
        }
    }

    @Test
    fun `with capacity of 1 throw when 2nd is requested`() {
        val rateLimiter = RateLimiter(1, 1)

        rateLimiter.request()

        assertFailsWith<RateLimiter.Error.Overflow> { rateLimiter.request() }
    }

    @Test
    fun `throw when creating limiter with delay less than 0`() {
        assertFails { RateLimiter(1, -1) }
    }

    @Test
    fun `reset to 0 after given delay`() {
        val rateLimiter = RateLimiter(1, 1_000)

        rateLimiter.request()

        assertFails { rateLimiter.request() }
    }
}
