package xyz.argent.candidateassessment.balance

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import xyz.argent.candidateassessment.tokens.Token

interface ObserveBalances : (List<Token>) -> Flow<List<Balance>>, RefreshBalances

class ObserveBalancesImpl @Inject constructor(
    private val observeTokenBalance: ObserveTokenBalance,
    private val refreshBalances: RefreshBalances,
) : ObserveBalances, RefreshBalances by refreshBalances {
    override fun invoke(tokens: List<Token>) =
        combine<Balance, List<Balance>>(
            tokens
                .map { token ->
                    observeTokenBalance(token)
                        .map { balance -> Balance(token, balance) }
                },
            Array<Balance>::toList,
        )
}
