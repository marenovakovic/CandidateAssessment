package balances

import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.balance.GetBalances
import xyz.argent.candidateassessment.balance.GetBalancesImpl
import xyz.argent.candidateassessment.balance.GetTokenBalance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val tenTokens = tokens.take(10)

class GetBalancesTest {

    private fun getBalances(
        getTokenBalance: GetTokenBalance = GetTokenBalance { Result.success("1234") },
    ): GetBalances = GetBalancesImpl(getTokenBalance)

    @Test
    fun `for empty token list return empty balance list`() = runTest {
        val getBalances = getBalances()

        val balances = getBalances(emptyList())

        assertEquals(emptyList(), balances)
    }

    @Test
    fun `for one token return one balance`() = runTest {
        val token = tenTokens.first()
        val getBalances = getBalances()

        val balances = getBalances(listOf(token))

        assertEquals(1, balances.size)
        assertEquals(token, balances.single().token)
    }

    @Test
    fun `error getting balance for single`() = runTest {
        val token = tenTokens.first()
        val getBalances = getBalances { Result.failure(Throwable()) }

        val balances = getBalances(listOf(token))

        assertEquals(1, balances.size)
        assertTrue(balances.single().balance.isFailure)
    }

    @Test
    fun `error getting balance for all tokens`() = runTest {
        val tokens = tenTokens
        val getBalances = getBalances { Result.failure(Throwable()) }

        val balances = getBalances(tokens)

        assertEquals(tokens.size, balances.size)
        assertTrue(balances.map { it.balance }.all { it.isFailure })
    }
}
