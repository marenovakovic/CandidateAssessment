package xyz.argent.candidateassessment.balance

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import xyz.argent.candidateassessment.tokens.Token

@Immutable
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
