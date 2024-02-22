package balances

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.balance.EtherscanApi
import xyz.argent.candidateassessment.balance.GetTokenBalanceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTokenBalanceTest {

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

    @Test
    fun `retry when max limit is reached`() = runTest {
        val token = tokens.first()
        var fail = true
        val failure = EtherscanApi.TokenBalanceResponse.MaxLimitReached
        val balance = "123456789"
        val success = EtherscanApi.TokenBalanceResponse(1, "", balance)
        val api = spyk(
            object : EtherscanApi {
                override suspend fun getTokenBalance(
                    contractAddress: String,
                    address: String,
                    apiKey: String,
                ) =
                    if (fail) failure.also { fail = false }
                    else success
            },
        )
        val getTokenBalance = GetTokenBalanceImpl(api)

        assertEquals(Result.success(balance), getTokenBalance(token))

        coVerify(exactly = 2) {
            api.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
    }

    @Test
    fun `returns failure for every error other than MaxLimitReached`() = runTest {
        val token = tokens.first()
        val failure = EtherscanApi.TokenBalanceResponse(0, "", "error")
        val api = spyk(
            object : EtherscanApi {
                override suspend fun getTokenBalance(
                    contractAddress: String,
                    address: String,
                    apiKey: String,
                ) = failure
            },
        )
        val getTokenBalance = GetTokenBalanceImpl(api)

        assertTrue(getTokenBalance(token).isFailure)

        coVerify(exactly = 1) {
            api.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
    }
}
