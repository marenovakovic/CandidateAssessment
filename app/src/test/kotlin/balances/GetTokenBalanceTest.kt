@file:OptIn(ExperimentalTime::class)

package balances

import io.mockk.coVerify
import io.mockk.spyk
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import tokens.tokens
import xyz.argent.candidateassessment.app.Constants
import xyz.argent.candidateassessment.balance.BackoffTimeMillis
import xyz.argent.candidateassessment.balance.CurrentTimeMillis
import xyz.argent.candidateassessment.balance.EtherscanApi
import xyz.argent.candidateassessment.balance.GetTokenBalance
import xyz.argent.candidateassessment.balance.GetTokenBalanceImpl
import xyz.argent.candidateassessment.balance.persistence.BalanceEntity
import xyz.argent.candidateassessment.balance.persistence.BalancesDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class GetTokenBalanceTest {

    private fun getTokenBalance(
        api: EtherscanApi = EtherscanApiMock,
        balancesDao: BalancesDao = BalancesDaoFake(),
        backoffTimeMillis: BackoffTimeMillis = BackoffTimeMillis(0),
        currentTimeMillis: CurrentTimeMillis = CurrentTimeMillis { 0 },
    ): GetTokenBalance =
        GetTokenBalanceImpl(
            api = api,
            balancesDao = balancesDao,
            backoffTimeMillis = backoffTimeMillis,
            currentTimeMillis = currentTimeMillis,
        )

    @Test
    fun `retry when max limit is reached`() = runTest {
        val token = tokens.first()
        val balance = "123456789"
        val failure = EtherscanApi.TokenBalanceResponse.MaxLimitReached
        val success = EtherscanApi.TokenBalanceResponse(1, "", balance)
        var fail = true
        val api = spyk(
            object : EtherscanApi {
                override suspend fun getTokenBalance(
                    contractAddress: String,
                    address: String,
                    apiKey: String,
                ) =
                    if (fail) failure.also { fail = false }
                    else success
            },
        )
        val getTokenBalance = getTokenBalance(api = api)

        assertEquals(Result.success(balance), getTokenBalance(token))

        coVerify(exactly = 2) {
            api.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
    }

    @Test
    fun `delay backoff time - (now - last request time) before retrying in case of max limit reached`() =
        runTest {
            val currentTimeMillis = AtomicLong(0)
            val delayInMillis = 1_000L
            val clockTick = 100
            val token = tokens.first()
            val balance = "123456789"
            val failure = EtherscanApi.TokenBalanceResponse.MaxLimitReached
            val success = EtherscanApi.TokenBalanceResponse(1, "", balance)
            var fail = true
            val api = spyk(
                object : EtherscanApi {
                    override suspend fun getTokenBalance(
                        contractAddress: String,
                        address: String,
                        apiKey: String,
                    ) =
                        if (fail) failure.also { fail = false }
                        else success
                },
            )
            val getTokenBalance = getTokenBalance(
                api = api,
                backoffTimeMillis = BackoffTimeMillis(delayInMillis),
                currentTimeMillis = {
                    currentTimeMillis.set(currentTimeMillis.get() + clockTick)
                    currentTimeMillis.get()
                },
            )

            launch {
                val duration = testScheduler.timeSource.measureTime {
                    getTokenBalance(token)
                }

                assertEquals(
                    delayInMillis - clockTick,
                    duration.inWholeMilliseconds,
                )
            }
        }

    @Test
    fun `returns failure for every error other than MaxLimitReached`() = runTest {
        val token = tokens.first()
        val failure = EtherscanApi.TokenBalanceResponse(0, "", "error")
        val api = spyk(
            object : EtherscanApi {
                override suspend fun getTokenBalance(
                    contractAddress: String,
                    address: String,
                    apiKey: String,
                ) = failure
            },
        )
        val getTokenBalance = getTokenBalance(api = api)

        assertTrue(getTokenBalance(token).isFailure)

        coVerify(exactly = 1) {
            api.getTokenBalance(
                token.address,
                Constants.walletAddress,
                Constants.etherscanApiKey,
            )
        }
    }

    @Test
    fun `if balance for given token is already saved don't fetch it`() = runTest {
        val token = tokens.first()
        val balance = "123456789"
        val api = spyk(EtherscanApiMock)
        val balancesDao = spyk(BalancesDaoFake())
        val getTokenBalance = getTokenBalance(api = api, balancesDao = balancesDao)

        balancesDao.saveBalance(BalanceEntity(token.address, balance))

        getTokenBalance(token)

        coVerify(exactly = 0) {
            api.getTokenBalance(any(), any(), any())
        }
        coVerify(exactly = 1) {
            balancesDao.getBalance(token.address)
        }
    }

    @Test
    fun `save fetched balance for given token if it's not already saved`() = runTest {
        val token = tokens.first()
        val balance = "123456789"
        val api = spyk(EtherscanApiMock)
        val balancesDao = spyk(BalancesDaoFake())
        val getTokenBalance = getTokenBalance(api = api, balancesDao = balancesDao)

        balancesDao.saveBalance(BalanceEntity(token.address, balance))

        getTokenBalance(token)

        assertEquals(
            balance,
            balancesDao.balances.value[token.address],
        )

        coVerify(exactly = 0) {
            api.getTokenBalance(any(), any(), any())
        }
        coVerify(exactly = 1) {
            balancesDao.getBalance(token.address)
        }
        coVerify(exactly = 1) {
            balancesDao.saveBalance(BalanceEntity(token.address, balance))
        }
    }

    @Test
    fun `fetch balance for tokens for which saved balance is empty string`() = runTest {
        val token = tokens.first()
        val api = spyk(EtherscanApiMock)
        val balancesDao = spyk(BalancesDaoFake())
        val getTokenBalance = getTokenBalance(api = api, balancesDao = balancesDao)

        balancesDao.saveBalance(BalanceEntity(token.address, ""))

        getTokenBalance(token)

        coVerify(exactly = 1) {
            api.getTokenBalance(token.address, any(), any())
        }
        coVerify(exactly = 1) {
            balancesDao.getBalance(token.address)
        }
    }
}
