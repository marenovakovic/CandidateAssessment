package xyz.argent.candidateassessment

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import xyz.argent.candidateassessment.tokens.persistence.TokenEntity
import xyz.argent.candidateassessment.tokens.persistence.TokensDao

@Database(entities = [TokenEntity::class, BalanceEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun tokensDao(): TokensDao
    abstract fun balancesDao(): BalancesDao
}
