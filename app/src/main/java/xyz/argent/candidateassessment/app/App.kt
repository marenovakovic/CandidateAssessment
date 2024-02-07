package xyz.argent.candidateassessment.app

import android.app.Application

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
