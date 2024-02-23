package balances

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao

class BalancesDaoFake : BalancesDao {
    val balances = MutableStateFlow(emptyMap<String, String>())

    override suspend fun getAllBalances(): List<BalanceEntity> =
        balances
            .value
            .map { (tokenAddress, rawBalance) ->
                BalanceEntity(tokenAddress, rawBalance)
            }

    override suspend fun getBalance(tokenAddress: String): BalanceEntity? {
        val balance = balances.value[tokenAddress] ?: return null
        return BalanceEntity(tokenAddress, balance)
    }

    override fun observeBalance(tokenAddress: String): Flow<BalanceEntity?> =
        balances.map {
            BalanceEntity(tokenAddress, it[tokenAddress].orEmpty())
        }

    override suspend fun saveBalance(balanceEntity: BalanceEntity) =
        balances.update { it + (balanceEntity.tokenAddress to balanceEntity.rawBalance) }
}
