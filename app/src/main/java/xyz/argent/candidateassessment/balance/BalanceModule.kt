package xyz.argent.candidateassessment.balance

import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
interface BalanceModule {

    @Binds
    fun getTokenBalance(impl: GetTokenBalanceImpl): GetTokenBalance

    @Binds
    fun getBalances(impl: GetBalancesImpl): GetBalances

    @Binds
    fun observeTokenBalance(impl: ObserveTokenBalanceImpl): ObserveTokenBalance

    @Binds
    fun observeBalances(impl: ObserveBalancesImpl): ObserveBalances

    companion object {
        @Singleton
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
    }
}
