@file:Suppress("SuspendFunctionOnCoroutineScope")

package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import xyz.argent.candidateassessment.tokens.Token

fun interface RefreshBalances {
    suspend fun refresh(tokens: List<Token>)
}

class RefreshBalancesImpl @Inject constructor(
    private val getTokenBalance: GetTokenBalance,
) : RefreshBalances {
    override suspend fun refresh(tokens: List<Token>) = coroutineScope {
        tokens.forEach { getTokenBalance(it) }
    }
}
