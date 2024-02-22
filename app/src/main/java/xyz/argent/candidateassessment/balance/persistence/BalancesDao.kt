package xyz.argent.candidateassessment.balance.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BalancesDao {

    @Query("SELECT * from balances")
    suspend fun getAllBalances(): List<BalanceEntity>?

    @Query("SELECT * FROM balances WHERE tokenAddress = :tokenAddress")
    fun observeBalance(tokenAddress: String): Flow<BalanceEntity?>

    @Insert
    suspend fun saveBalance(balanceEntity: BalanceEntity)
}
