package tokens

import app.cash.turbine.test
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.tokens.GetTokens
import xyz.argent.candidateassessment.tokens.ObserveTokens
import xyz.argent.candidateassessment.tokens.ObserveTokensImpl
import xyz.argent.candidateassessment.tokens.TokensDao
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTokensTest {

    private fun observeTokens(
        tokensDao: TokensDao = TokensDaoFake(),
        getTokens: GetTokens = GetTokens { Result.success(tokens.take(10)) },
    ): ObserveTokens = ObserveTokensImpl(tokensDao, getTokens)

    @Test
    fun `observe tokens from TokensDao`() = runTest {
        val tokens = tokens.take(10)
        val tokensDao = TokensDaoFake(tokens)
        val observeTokens = observeTokens(tokensDao)

        observeTokens().test {
            assertEquals(tokens, awaitItem())
        }
    }

    @Test
    fun `fetch new tokens and save them to TokensDao`() = runTest {
        val tokens = tokens.take(10)
        val tokensDao = spyk(TokensDaoFake())
        val getTokens = GetTokens {
            delay(1_000)
            Result.success(tokens)
        }

        val observeTokens = observeTokens(tokensDao, getTokens)

        observeTokens().test {
            assertEquals(tokens, awaitItem())
        }
    }
}
