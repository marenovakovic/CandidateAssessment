package xyz.argent.candidateassessment.app

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.argent.candidateassessment.CloseableCoroutineScope
import xyz.argent.candidateassessment.Database
import xyz.argent.candidateassessment.connectivity.ConnectivityObserver
import xyz.argent.candidateassessment.connectivity.ConnectivityObserverImpl

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    fun connectivityObserver(impl: ConnectivityObserverImpl): ConnectivityObserver

    companion object {
        @Singleton
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

        @Singleton
        @Provides
        fun database(@ApplicationContext context: Context) =
            Room
                .databaseBuilder(context, Database::class.java, "database")
                .fallbackToDestructiveMigration()
                .build()

        @Singleton
        @Provides
        fun tokensDao(database: Database) = database.tokensDao()

        @Singleton
        @Provides
        fun balancesDao(database: Database) = database.balancesDao()
    }
}
