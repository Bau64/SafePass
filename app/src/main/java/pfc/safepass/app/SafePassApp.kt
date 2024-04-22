package pfc.safepass.app

import android.app.Application

class SafePassApp : Application() {
    // Inicializa las preferencias cuando inicia la aplicacion

    companion object{
        lateinit var prefs: Preferences
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = Preferences(applicationContext)
    }
}