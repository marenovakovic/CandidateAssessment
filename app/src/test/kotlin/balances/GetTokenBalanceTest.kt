package balances

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.balance.GetTokenBalanceImpl
import kotlin.test.Test

class GetTokenBalanceTest {

    @Test
    fun `calls api`() = runTest {
        val token = tokens.first()
        val api = spyk(EtherscanApiMock)
        val getTokenBalance = GetTokenBalanceImpl(api)

        getTokenBalance(token)

        coVerify(exactly = 1) {
            EtherscanApiMock.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
    }
}
