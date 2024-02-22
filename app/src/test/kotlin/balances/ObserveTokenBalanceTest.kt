package balances

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.balance.GetTokenBalance
import xyz.argent.candidateassessment.balance.ObserveTokenBalanceImpl
import xyz.argent.candidateassessment.balance.ObserveTokenBalance
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveTokenBalanceTest {

    private fun observeBalance(
        balancesDao: BalancesDao = BalancesDaoFake(),
        getTokenBalance: GetTokenBalance = GetTokenBalance { Result.success("1234") },
    ): ObserveTokenBalance = ObserveTokenBalanceImpl(balancesDao, getTokenBalance)

    @Test
    fun `return balance from BalancesDao`() = runTest {
        val token = tokens.first()
        val rawBalance = "123456789"
        val balanceEntity = BalanceEntity(token.address, rawBalance)
        val balancesDao = BalancesDaoFake()

        val observeBalance = observeBalance(balancesDao = balancesDao)
        balancesDao.saveBalance(balanceEntity)

        observeBalance(token).test {
            assertEquals(Result.success(rawBalance), awaitItem())
        }
    }

    @Test
    fun `when balance for token doesn't exist in BalancesDao get it`() = runTest {
        val token = tokens.first()
        val balance = "123456789"

        val observeBalance = observeBalance {
            Result.success(balance)
        }

        observeBalance(token).test {
            assertEquals(Result.success(null), awaitItem())
            assertEquals(Result.success(balance), awaitItem())
        }
    }

    @Test
    fun `when GetTokenBalance fails save empty string as rawBalance`() = runTest {
        val token = tokens.first()
        val balancesDao = BalancesDaoFake()

        val observeBalance = observeBalance(balancesDao = balancesDao) {
            Result.failure(Throwable())
        }

        observeBalance(token).test {
            cancelAndIgnoreRemainingEvents()

            assertEquals("", balancesDao.balances.value[token.address])
        }
    }

    @Test
    fun `when balance is empty string return Result failure`() = runTest {
        val token = tokens.first()

        val observeBalance = observeBalance {
            Result.success("")
        }

        observeBalance(token).test {
            assertEquals(Result.success(null), awaitItem())
            assertTrue(awaitItem().isFailure)
        }
    }
}
