package balances

import java.math.BigDecimal
import tokens.tokens
import xyz.argent.candidateassessment.balance.Balance
import kotlin.test.Test
import kotlin.test.assertEquals

class BalanceTest {
    private val token = tokens.first()

    @Test
    fun `token 1 relevant decimal and balance 0 should be 0,0`() =
        assertFormatted(1, "0", "0")

    @Test
    fun `token 1 relevant decimal and balance 11 should be 1,1`() =
        assertFormatted(1, "11", "1.1")

    @Test
    fun `token with 1 relevant decimals and balance 1 should be 0,1`() =
        assertFormatted(1, "1", "0.1")

    @Test
    fun `token 1 relevant decimal and balance 111 should be 11,1`() =
        assertFormatted(1, "111", "11.1")

    @Test
    fun `token with 2 relevant decimals and balance 111 should be 1,11`() =
        assertFormatted(2, "111", "1.11")

    @Test
    fun `token with 2 relevant decimals and balance 1111 should be 11,11`() =
        assertFormatted(2, "1111", "11.11")

    @Test
    fun `token with 4 relevant decimals and balance 1111 should be 0,1111`() =
        assertFormatted(4, "1111", "0.1111")

    @Test
    fun `token with 4 relevant decimals and balance 1 should be 0,0001`() =
        assertFormatted(4, "1", "0.0001")

    @Test
    fun `token with 4 relevant decimals and balance 0 should be 0,0000`() =
        assertFormatted(4, "0", "0")

    @Test
    fun `token with 6 relevant decimals and balance 123456789 should be 123,456789`() =
        assertFormatted(6, "123456789", "123.456789")

    @Test
    fun `token with 6 relevant decimals and balance 123456 should be 0,123456`() =
        assertFormatted(6, "123456", "0.123456")

    private fun assertFormatted(
        decimals: Int,
        rawBalance: String,
        expectedBalance: String,
    ) {
        val token = token.copy(decimals = decimals)
        val tokenBalance = Result.success(rawBalance)

        val balance = Balance(token, tokenBalance).also { println(it.balance) }

        assertEquals(Result.success(BigDecimal(expectedBalance)), balance.balance)
    }
}
