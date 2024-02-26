package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import xyz.argent.candidateassessment.tokens.Token

fun interface ObserveTokenBalance : (Token) -> Flow<Result<String?>>

class ObserveTokenBalanceImpl @Inject constructor(
    private val balancesDao: BalancesDao,
) : ObserveTokenBalance {
    override fun invoke(token: Token): Flow<Result<String?>> =
        balancesDao
            .observeBalance(token.address)
            .map {
                if (it?.rawBalance?.isBlank() == true) Result.failure(Throwable())
                else Result.success(it?.rawBalance)
            }
}
