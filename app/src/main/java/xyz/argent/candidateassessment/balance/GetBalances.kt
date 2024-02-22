package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import xyz.argent.candidateassessment.tokens.Token

fun interface GetBalances : suspend (List<Token>) -> List<Balance>

@Suppress("SuspendFunctionOnCoroutineScope")
class GetBalancesImpl @Inject constructor(
    private val getTokenBalance: GetTokenBalance,
) : GetBalances {

    override suspend operator fun invoke(tokens: List<Token>) = coroutineScope {
        tokens
            .map { getBalance(it) }
            .awaitAll()
            .map { (token, balance) ->
                Balance(token = token, balance = balance)
            }
    }

    private suspend fun CoroutineScope.getBalance(token: Token) =
        async { token to getTokenBalance(token) }
}
