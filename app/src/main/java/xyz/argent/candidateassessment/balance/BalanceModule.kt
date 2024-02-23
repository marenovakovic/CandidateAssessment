package xyz.argent.candidateassessment.balance

import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
interface BalanceModule {

    @Binds
    fun getBalances(impl: GetBalancesImpl): GetBalances

    @Binds
    fun refreshBalances(impl: RefreshBalancesImpl): RefreshBalances

    @Binds
    fun observeTokenBalance(impl: ObserveTokenBalanceImpl): ObserveTokenBalance

    @Binds
    fun observeBalances(impl: ObserveBalancesImpl): ObserveBalances

    companion object {
        @Provides
        fun etherscanApi(okHttpClient: OkHttpClient, moshi: Moshi) =
            Retrofit.Builder()
                .baseUrl("https://api.etherscan.io/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(EtherscanApi::class.java)

        @Provides
        fun getBalancesRateLimit() = GetBalancesRateLimit.FivePerSecond

        @Provides
        fun currentTimeMillis(): CurrentTimeMillis = CurrentTimeMillisImpl

        @Provides
        fun getTokenBalance(
            etherscanApi: EtherscanApi,
            currentTimeMillis: CurrentTimeMillis,
        ): GetTokenBalance =
            GetTokenBalanceImpl(
                etherscanApi,
                BackoffTimeMillis.EtherscanApiBackoffTime,
                currentTimeMillis,
            )
    }
}
