package xyz.argent.candidateassessment.tokens.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TokensDao {
    @Query("SELECT * FROM tokens")
    fun getAllTokens(): List<TokenEntity>

    @Query("SELECT * FROM tokens")
    fun observeAllTokens(): Flow<List<TokenEntity>>

    @Query("SELECT * FROM tokens WHERE address = :address")
    suspend fun getToken(address: String): TokenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTokens(tokens: List<TokenEntity>)
}
