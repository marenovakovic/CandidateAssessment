package balances

import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.balance.GetTokenBalance
import xyz.argent.candidateassessment.balance.RefreshBalancesImpl
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshBalancesTest {

    @Test
    fun `when no balance is saved get and save balances for all tokens`() = runTest {
        val balancesDao = BalancesDaoFake()
        val balance = "1234"
        val getTokenBalance = GetTokenBalance { Result.success(balance) }
        val refreshBalances = RefreshBalancesImpl(balancesDao, getTokenBalance)

        val tokens = tokens.first()
        refreshBalances.refresh(listOf(tokens))

        assertEquals(balancesDao.balances.value[tokens.address], balance)
    }

    @Test
    fun `when requesting refresh for token whose balance is already saved, do nothing`() = runTest {
        val balancesDao = spyk(BalancesDaoFake())
        val token = tokens.first()
        val balance = "1234"
        val getTokenBalance = GetTokenBalance { Result.success(balance) }
        val refreshBalances = RefreshBalancesImpl(balancesDao, getTokenBalance)

        balancesDao.saveBalance(BalanceEntity(token.address, balance))
        clearMocks(balancesDao)

        refreshBalances.refresh(listOf(token))

        coVerify(exactly = 0) {
            balancesDao.saveBalance(any())
        }
    }

    @Test
    fun `get and save balance only for tokens which don't have saved balance`() = runTest {
        val balancesDao = spyk(BalancesDaoFake())
        val tokens = tokens.take(2)
        val balance = "1234"
        val getTokenBalance = GetTokenBalance { Result.success(balance) }
        val refreshBalances = RefreshBalancesImpl(balancesDao, getTokenBalance)

        balancesDao.saveBalance(BalanceEntity(tokens.first().address, balance))
        clearMocks(balancesDao)

        refreshBalances.refresh(tokens)

        coVerify(exactly = 1) {
            balancesDao.saveBalance(BalanceEntity(tokens.last().address, balance))
        }
    }
}
