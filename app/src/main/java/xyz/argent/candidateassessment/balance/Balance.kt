package xyz.argent.candidateassessment.balance

import xyz.argent.candidateassessment.tokens.Token

data class Balance(
    val token: Token,
    val balance: Double,
)
