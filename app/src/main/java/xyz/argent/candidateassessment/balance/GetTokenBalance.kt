package xyz.argent.candidateassessment.balance

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.tokens.Token

fun interface GetTokenBalance : suspend (Token) -> Result<String>
fun interface CurrentTimeMillis : () -> Long

val CurrentTimeMillisImpl = CurrentTimeMillis { System.currentTimeMillis() }

@JvmInline
value class BackoffTimeMillis(val value: Long) {
    companion object {
        val EtherscanApiBackoffTime = BackoffTimeMillis(1_000)
    }
}

class GetTokenBalanceImpl @Inject constructor(
    private val api: EtherscanApi,
    private val backoffTimeMillis: BackoffTimeMillis = BackoffTimeMillis(1_000),
    private val currentTimeMillis: CurrentTimeMillis = CurrentTimeMillisImpl,
) : GetTokenBalance {
    private val mutex = Mutex()
    private val last = AtomicLong(0)

    override suspend fun invoke(token: Token): Result<String> =
        runCatching {
            mutex.withLock {
                last.set(currentTimeMillis())
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
                            delay(backoffTimeMillis.value - (currentTimeMillis() - last.get()))
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
