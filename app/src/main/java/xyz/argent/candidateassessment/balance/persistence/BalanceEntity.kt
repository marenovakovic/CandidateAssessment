package xyz.argent.candidateassessment.balance.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("balances")
data class BalanceEntity(
    @PrimaryKey
    val tokenAddress: String,
    val rawBalance: String,
)
