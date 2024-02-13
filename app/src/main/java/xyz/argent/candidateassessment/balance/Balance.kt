package xyz.argent.candidateassessment.balance

import java.math.RoundingMode
import xyz.argent.candidateassessment.tokens.Token

data class Balance private constructor(
    val token: Token,
    val balance: Result<Double>,
) {

    companion object {
        operator fun invoke(token: Token, balance: Result<Double>) =
            Balance(
                token,
                balance
                    .map {
                        it
                            .toBigDecimal()
                            .setScale(token.decimals?.toInt() ?: 0, RoundingMode.HALF_EVEN)
                            .toDouble()
                    },
            )
    }
}
