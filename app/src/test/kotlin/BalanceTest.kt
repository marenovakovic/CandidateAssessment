import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.tokens.tokens
import kotlin.test.Test
import kotlin.test.assertEquals

class BalanceTest {
    private val token = tokens.first()

    @Test
    fun `token 1 relevant decimal and balance 1,1 should be 1,1`() =
        assertRounded(1.0, 1.1, 1.1)

    @Test
    fun `token with 1 relevant decimals and balance 1,0 should be 1,0`() =
        assertRounded(1.0, 1.0, 1.0)

    @Test
    fun `token 1 relevant decimal and balance 1,11 should be 1,1`() =
        assertRounded(1.0, 1.11, 1.1)

    @Test
    fun `token with 2 relevant decimals and balance 1,11 should be 1,11`() =
        assertRounded(2.0, 1.11, 1.11)

    @Test
    fun `token with 2 relevant decimals and balance 1,111 should be 1,11`() =
        assertRounded(2.0, 1.111, 1.11)

    @Test
    fun `token with 4 relevant decimals and balance 1,111 should be 1,1110`() =
        assertRounded(4.0, 1.111, 1.1110)

    @Test
    fun `token with 4 relevant decimals and balance 1,0 should be 1,0000`() =
        assertRounded(4.0, 1.0, 1.0000)

    private fun assertRounded(
        relevantDecimals: Double,
        balanceDouble: Double,
        expectedBalance: Double,
    ) {
        val token = token.copy(decimals = relevantDecimals)
        val tokenBalance = Result.success(balanceDouble)

        val balance = Balance(token, tokenBalance)

        assertEquals(Result.success(expectedBalance), balance.balance)
    }
}
