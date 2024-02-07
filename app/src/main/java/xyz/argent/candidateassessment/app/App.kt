package xyz.argent.candidateassessment.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        dependencies = Dependencies(this)
    }

    companion object {
        lateinit var dependencies: Dependencies
            private set
    }
}
