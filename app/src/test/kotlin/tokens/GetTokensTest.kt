package tokens

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.tokens.EthExplorerApi
import xyz.argent.candidateassessment.tokens.GetTokensImpl
import xyz.argent.candidateassessment.tokens.persistence.toTokenEntity
import xyz.argent.candidateassessment.tokens.toToken
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTokensTest {

    @Test
    fun `get and save tokens from the api if they aren't already saved`() = runTest {
        val tokenResponse = tokensResponse
        val tokenEntities =
            tokenResponse
                .tokens
                .map { it.toToken() }
                .map { it.toTokenEntity() }
        val api = spyk<EthExplorerApi> {
            coEvery { getTopTokens() } returns tokenResponse
        }
        val tokensDao = spyk<TokensDaoFake>(TokensDaoFake(emptyList()))
        val getTokens = GetTokensImpl(api, tokensDao)

        assertEquals(emptyList(), tokensDao.tokens.value)

        val tokens = getTokens().getOrThrow()

        assertEquals(tokenResponse.tokens.map(EthExplorerApi.TokenResponse::toToken), tokens)
        assertEquals(tokenEntities, tokensDao.tokens.value)

        coVerify(exactly = 1) { api.getTopTokens() }
        coVerify(exactly = 1) { tokensDao.getAllTokens() }
        coVerify(exactly = 1) { tokensDao.saveTokens(tokenEntities) }
    }

    @Test
    fun `doesn't call the api if tokens are saved and doesn't save tokens`() = runTest {
        val tokens = tokens.take(2)
        val tokenEntities = tokens.map { it.toTokenEntity() }
        val api = spyk<EthExplorerApi>()
        val tokensDao = spyk<TokensDaoFake>(TokensDaoFake(tokens.take(2)))
        val getTokens = GetTokensImpl(api, tokensDao)

        assertEquals(tokenEntities, tokensDao.tokens.value)

        getTokens().getOrThrow()

        assertEquals(tokenEntities, tokensDao.tokens.value)

        coVerify(exactly = 0) { api.getTopTokens() }
        coVerify(exactly = 1) { tokensDao.getAllTokens() }
        coVerify(exactly = 0) { tokensDao.saveTokens(tokenEntities) }
    }
}
