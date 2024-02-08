import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.tokens.EthExplorerApi
import xyz.argent.candidateassessment.tokens.GetTokens
import xyz.argent.candidateassessment.tokens.toToken
import xyz.argent.candidateassessment.tokens.tokensResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTokensTest {

    @Test
    fun `get tokens from the api`() = runTest {
        val tokenResponse = tokensResponse
        val spy = spyk<EthExplorerApi> {
            coEvery { getTopTokens() } returns tokenResponse
        }
        val getTokens = GetTokens(spy)

        val tokens = getTokens()

        assertEquals(tokenResponse.tokens.map(EthExplorerApi.TokenResponse::toToken), tokens)
        coVerify(exactly = 1) { spy.getTopTokens() }
    }
}
