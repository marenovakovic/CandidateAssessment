package xyz.argent.candidateassessment.tokens.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey
    val address: String,
    val name: String?,
    val symbol: String?,
    val decimals: Double?,
    val image: String?,
)
