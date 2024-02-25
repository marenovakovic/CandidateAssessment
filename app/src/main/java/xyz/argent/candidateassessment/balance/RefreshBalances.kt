@file:Suppress("SuspendFunctionOnCoroutineScope")

package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import xyz.argent.candidateassessment.tokens.Token

fun interface RefreshBalances {
    suspend fun refresh(tokens: List<Token>)
}

class RefreshBalancesImpl @Inject constructor(
    private val balancesDao: BalancesDao,
    private val getTokenBalance: GetTokenBalance,
) : RefreshBalances {
    override suspend fun refresh(tokens: List<Token>) = coroutineScope {
        getBalances(tokens)
    }

    private suspend fun CoroutineScope.getBalances(tokens: List<Token>) {
        tokens
            .filterTokensWithNoBalanceSaved()
            .map { getBalance(it) }
            .awaitAll()
            .onEach {
                val (token, balance) = it
                coroutineScope {
                    launch { saveBalance(token, balance) }
                }
            }
    }

    private suspend fun List<Token>.filterTokensWithNoBalanceSaved(): List<Token> {
        val a =
            balancesDao
                .getAllBalances()
                .orEmpty()
                .filter { it.rawBalance.isNullOrBlank() }
                .map { it.tokenAddress }

        return filter { it.address in a }
    }

    private suspend fun CoroutineScope.getBalance(token: Token) =
        async { token to getTokenBalance(token) }

    private suspend fun saveBalance(token: Token, balance: Result<String>) {
        val balanceEntity = BalanceEntity(token.address, balance.getOrThrow())
        println("saving balance for ${token.address}")
        balancesDao.saveBalance(balanceEntity)
    }
}
