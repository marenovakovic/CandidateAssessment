package balances

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.ObserveBalances
import xyz.argent.candidateassessment.balance.ObserveBalancesImpl
import xyz.argent.candidateassessment.balance.ObserveTokenBalance
import xyz.argent.candidateassessment.balance.RefreshBalances
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveBalancesTest {

    private fun observeBalances(
        observeTokenBalance: () -> Result<String> = { Result.success("1234") },
    ): ObserveBalances =
        ObserveBalancesImpl(
            observeTokenBalance = { flowOf(observeTokenBalance()) },
            refreshBalances = {},
        )

    @Test
    fun `invoke`() {
        val observeBalances = observeBalances()

        observeBalances(emptyList())
    }

    @Test
    fun `for empty tokens list just completes`() = runTest {
        val observeBalances = observeBalances()

        observeBalances(emptyList()).test {
            awaitComplete()
        }
    }

    @Test
    fun `for list of one token return flow with list of one balance`() = runTest {
        val tokens = tokens.take(1)
        val balance = Result.success("1234")
        val observeBalances = observeBalances { balance }

        observeBalances(tokens).test {
            assertEquals(listOf(Balance(tokens.single(), balance)), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `for list of two tokens return flow with list of two balances`() = runTest {
        val tokens = tokens.take(2)
        val balance = Result.success("1234")
        val observeBalances = observeBalances { balance }

        observeBalances(tokens).test {
            val expectedBalances = tokens.map { Balance(it, balance) }
            assertEquals(expectedBalances, awaitItem())
            awaitComplete()
        }
    }
}
