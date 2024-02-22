package xyz.argent.candidateassessment.balance

import java.math.BigDecimal
import xyz.argent.candidateassessment.tokens.Token

data class Balance private constructor(
    val token: Token,
    val balance: Result<BigDecimal?>,
) {
    companion object {
        operator fun invoke(token: Token, balance: Result<String?>) =
            Balance(
                token,
                balance.map {
                    it
                        ?.toBigDecimal()
                        ?.divide(BigDecimal.TEN.pow(token.decimals?.toInt() ?: 1))
                },
            )
    }
}
