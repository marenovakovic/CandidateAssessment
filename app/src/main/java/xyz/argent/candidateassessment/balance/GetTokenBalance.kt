package xyz.argent.candidateassessment.balance

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import xyz.argent.candidateassessment.tokens.Token

fun interface GetTokenBalance : suspend (Token) -> Result<String?>
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
    private val balancesDao: BalancesDao,
    private val backoffTimeMillis: BackoffTimeMillis,
    private val currentTimeMillis: CurrentTimeMillis,
) : GetTokenBalance {
    private val mutex = Mutex()
    private val last = AtomicLong(0)

    override suspend fun invoke(token: Token): Result<String?> =
        balancesDao
            .getBalance(token.address)
            ?.takeIf { !it.rawBalance.isNullOrBlank() }
            ?.let { Result.success(it.rawBalance) }
            ?: fetchBalance(token)
                .also { balance -> saveBalance(token, balance) }

    private suspend fun fetchBalance(token: Token) =
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

    private suspend fun saveBalance(token: Token, balance: Result<String?>) {
        balancesDao.saveBalance(
            BalanceEntity(
                token.address,
                balance.getOrNull().orEmpty(),
            )
        )
    }
}
