package tokens

import xyz.argent.candidateassessment.tokens.TokenImageBaseUrl
import xyz.argent.candidateassessment.tokens.toToken
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenMapperTest {

    @Test
    fun `TokenResponse to Token`() {
        val response = tokensResponse.tokens.first()

        val token = response.toToken()

        assertEquals(response.address, token.address)
        assertEquals(response.name, token.name)
        assertEquals(response.symbol, token.symbol)
        assertEquals(response.decimals, token.decimals)
        assertEquals("$TokenImageBaseUrl/${response.image}", token.image)
    }

    @Test
    fun `token image link is empty if response image is null`() {
        val response = tokensResponse.tokens.first().copy(image = null)

        val token = response.toToken()

        assertEquals("", token.image)
    }
}
