package pfc.safepass.app.preferences

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SafePassApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Set app theme
        val prefs = Preferences(applicationContext)
        AppCompatDelegate.setDefaultNightMode(prefs.getAppTheme())
    }
}