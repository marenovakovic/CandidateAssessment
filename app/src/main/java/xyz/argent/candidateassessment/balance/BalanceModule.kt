package xyz.argent.candidateassessment.balance

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class BalanceModule {

    @Provides
    fun etherscanApi(okHttpClient: OkHttpClient, moshi: Moshi) =
        Retrofit.Builder()
            .baseUrl("https://api.etherscan.io/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EtherscanApi::class.java)
}