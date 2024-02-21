package tokens

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.tokens.EthExplorerApi
import xyz.argent.candidateassessment.tokens.GetTokensImpl
import xyz.argent.candidateassessment.tokens.toToken
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTokensTest {

    @Test
    fun `get tokens from the api`() = runTest {
        val tokenResponse = tokensResponse
        val spy = spyk<EthExplorerApi> {
            coEvery { getTopTokens() } returns tokenResponse
        }
        val getTokens = GetTokensImpl(spy)

        val tokens = getTokens().getOrThrow()

        assertEquals(tokenResponse.tokens.map(EthExplorerApi.TokenResponse::toToken), tokens)
        coVerify(exactly = 1) { spy.getTopTokens() }
    }
}
