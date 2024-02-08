import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.balance.EtherscanApiMock
import xyz.argent.candidateassessment.balance.GetTokenBalanceImpl
import xyz.argent.candidateassessment.tokens.tokens
import kotlin.test.Test

class GetTokenBalanceImplTest {

    @Test
    fun `calls api`() = runTest {
        val token = tokens.first()
        val api = spyk(EtherscanApiMock)
        val getTokenBalance = GetTokenBalanceImpl(api)

        getTokenBalance(token)

        coVerify(exactly = 1) {
            api.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
    }
}
