package xyz.argent.candidateassessment.app

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.ConnectivityObserverImpl

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    fun connectivityObserver(impl: ConnectivityObserverImpl): ConnectivityObserver

    companion object {
        @Provides
        fun okHttpClient(): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        }

        @Provides
        fun moshi() =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        @Provides
        fun closeableCoroutineScope() =
            CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}
