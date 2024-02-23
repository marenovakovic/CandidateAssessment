package xyz.argent.candidateassessment.balance.persistence

import androidx.room.Entity

@Entity("balances", primaryKeys = ["tokenAddress", "rawBalance"])
data class BalanceEntity(
    val tokenAddress: String,
    val rawBalance: String,
)
