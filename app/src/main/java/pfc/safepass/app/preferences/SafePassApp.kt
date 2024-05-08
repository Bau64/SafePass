package pfc.safepass.app.preferences

import android.app.Application
import pfc.safepass.app.preferences.Preferences

class SafePassApp : Application() {
    // Initializes preferences when app boots up

    companion object{
        lateinit var prefs: Preferences
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = Preferences(applicationContext)
    }
}