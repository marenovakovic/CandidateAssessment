package xyz.argent.candidateassessment.balance.persistence

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BalancesDao {

    @Query("SELECT * from balances")
    suspend fun getAllBalances(): List<BalanceEntity>?

    @Query("SELECT * FROM balances WHERE tokenAddress = :tokenAddress")
    suspend fun getBalance(tokenAddress: String): BalanceEntity?

    @Query("SELECT * FROM balances WHERE tokenAddress = :tokenAddress")
    fun observeBalance(tokenAddress: String): Flow<BalanceEntity?>

    @Upsert
    suspend fun saveBalances(balanceEntries: List<BalanceEntity>)

    @Upsert
    suspend fun saveBalance(balanceEntity: BalanceEntity)
}
