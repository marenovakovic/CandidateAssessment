package tokens

import app.cash.turbine.test
import balances.BalancesDaoFake
import kotlinx.coroutines.test.runTest
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import xyz.argent.candidateassessment.tokens.EthExplorerApi
import xyz.argent.candidateassessment.tokens.ObserveTokens
import xyz.argent.candidateassessment.tokens.ObserveTokensImpl
import xyz.argent.candidateassessment.tokens.persistence.TokensDao
import xyz.argent.candidateassessment.tokens.toToken
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTokensTest {

    private fun observeTokens(
        tokensDao: TokensDao = TokensDaoFake(),
        balancesDao: BalancesDao = BalancesDaoFake(),
        stubTokenResponses: () -> List<EthExplorerApi.TokenResponse> =
            { tokensResponse.tokens.take(2) },
    ): ObserveTokens = ObserveTokensImpl(
        tokensDao,
        balancesDao,
        EthExplorerApiStub { stubTokenResponses() },
    )

    @Test
    fun `observe tokens from TokensDao`() = runTest {
        val responses = tokensResponse.tokens.take(1)
        val tokens = responses.map { it.toToken() }
        val tokensDao = TokensDaoFake(tokens)

        val observeTokens = observeTokens(tokensDao) { responses }

        observeTokens().test {
            assertEquals(tokens, awaitItem())
        }
    }

    @Test
    fun `fetch and save tokens when TokensDao doesn't contain any tokens`() = runTest {
        val responses = tokensResponse.tokens.take(1)
        val tokens = responses.map { it.toToken() }
        val tokensDao = TokensDaoFake()

        val observeTokens = observeTokens(tokensDao) { responses }

        observeTokens().test {
            assertEquals(emptyList(), awaitItem())

            assertEquals(tokens, awaitItem())
        }
    }

    @Test
    fun `when TokensDao contains tokens don't refresh them`() = runTest {
        val responses = tokensResponse.tokens.take(1)
        val tokens = responses.map { it.toToken() }
        val tokensDao = TokensDaoFake(tokens)

        val observeTokens = observeTokens(tokensDao) { throw Throwable() }

        observeTokens().test {
            assertEquals(tokens, awaitItem())
        }
    }
}
