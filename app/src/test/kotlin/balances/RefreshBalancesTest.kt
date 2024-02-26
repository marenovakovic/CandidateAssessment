package balances

import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.balance.GetTokenBalance
import xyz.argent.candidateassessment.balance.RefreshBalancesImpl
import xyz.argent.candidateassessment.tokens.Token
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshBalancesTest {

    @Test
    fun `call GetTokenBalance for tokens`() = runTest {
        val tokens = tokens.take(10)
        val balance = "1234"
        val getTokenBalancesCalls = mutableListOf<Token>()
        val getTokenBalance = GetTokenBalance {
            getTokenBalancesCalls += it
            Result.success(balance)
        }
        val refreshBalances = RefreshBalancesImpl(getTokenBalance)

        refreshBalances.refresh(tokens)

        assertEquals(tokens, getTokenBalancesCalls)
    }
}
