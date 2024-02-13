package xyz.argent.candidateassessment.balance

import xyz.argent.candidateassessment.tokens.Token

data class Balance private constructor(
    val token: Token,
    val balance: Result<String>,
) {
    companion object {
        operator fun invoke(token: Token, balance: Result<Double>) =
            Balance(
                token,
                balance.map { String.format("%.${token.decimals?.toInt() ?: 0}f", it) },
            )
    }
}
