package xyz.argent.candidateassessment.tokens

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
interface TokensModule {

    @Binds
    fun getTokens(impl: GetTokensImpl): GetTokens

    companion object {
        @Provides
        fun ethExplorerApi(okHttpClient: OkHttpClient, moshi: Moshi) =
            Retrofit.Builder()
                .baseUrl("https://api.ethplorer.io/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(EthExplorerApi::class.java)
    }
}
