package xyz.argent.candidateassessment.balance

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.tokens.Token

fun interface GetTokenBalance : suspend (Token) -> Result<String>

class GetTokenBalanceImpl @Inject constructor(private val api: EtherscanApi) : GetTokenBalance {
    private val mutex = Mutex()
    private val last = AtomicLong(0)

    override suspend fun invoke(token: Token): Result<String> =
        runCatching {
            mutex.withLock {
                last.set(System.currentTimeMillis())
                api.getTokenBalance(
                    token.address,
                    Constants.walletAddress,
                    Constants.etherscanApiKey,
                )
            }
        }
            .fold(
                {
                    when {
                        it == EtherscanApi.TokenBalanceResponse.MaxLimitReached -> {
                            delay(1_000 - (System.currentTimeMillis() - last.get()))
                            invoke(token)
                        }
                        it.status == 0L && it.result != EtherscanApi.TokenBalanceResponse.MaxLimitReached.result ->
                            Result.failure(Throwable(it.result))
                        else -> Result.success(it.result)
                    }
                },
                { Result.failure(it) },
            )
}
