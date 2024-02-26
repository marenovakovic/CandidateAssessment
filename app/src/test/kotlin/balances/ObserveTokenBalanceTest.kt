package balances

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.balance.ObserveTokenBalance
import xyz.argent.candidateassessment.balance.ObserveTokenBalanceImpl
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveTokenBalanceTest {

    private fun observeBalance(balancesDao: BalancesDao = BalancesDaoFake()): ObserveTokenBalance =
        ObserveTokenBalanceImpl(balancesDao)

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
    fun `when balance is empty string return Result failure`() = runTest {
        val token = tokens.first()

        val balancesDao = BalancesDaoFake()
        balancesDao.saveBalance(BalanceEntity(token.address, ""))

        val observeBalance = observeBalance(balancesDao)

        observeBalance(token).test {
            assertTrue(awaitItem().isFailure)
        }
    }
}
