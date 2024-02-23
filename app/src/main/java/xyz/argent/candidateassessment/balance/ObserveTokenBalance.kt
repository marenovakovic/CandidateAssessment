package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import xyz.argent.candidateassessment.tokens.Token

fun interface ObserveTokenBalance : (Token) -> Flow<Result<String?>>

class ObserveTokenBalanceImpl @Inject constructor(
    private val balancesDao: BalancesDao,
    private val getTokenBalance: GetTokenBalance,
) : ObserveTokenBalance {
    override fun invoke(token: Token): Flow<Result<String?>> =
        balancesDao
            .observeBalance(token.address)
            .onEach {
                if (it == null)
                    refreshTokenBalance(token)
            }
            .map {
                if (it?.rawBalance?.isBlank() == true) Result.failure(Throwable())
                else Result.success(it?.rawBalance)
            }

    private suspend fun refreshTokenBalance(token: Token) {
        getTokenBalance(token)
            .fold(
                onSuccess = {
                    balancesDao.saveBalance(BalanceEntity(token.address, it))
                },
                onFailure = {
                    balancesDao.saveBalance(BalanceEntity(token.address, ""))
                },
            )
    }
}