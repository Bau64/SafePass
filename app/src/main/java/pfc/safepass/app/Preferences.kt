package pfc.safepass.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Preferences(context: Context) {
    private val PREFS_NAME = "pfc.safepass.mypreferences"
    private val prefs_masterPWD = "masterPWD"
    private val prefs_logged_status = "loggedStatus"
    private val prefs_session_start_time = "sessionStartTime"
    private val prefs_last_sessionTimeout = "last_timeout"
    private var prefs_timeout_active = "timeout_active"

    // Encriptar preferencias
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getLastSessionTimeout(): Int {
        return prefs.getInt("last_timeout", 5)
    }

    fun setTimeoutState(state: Boolean) {
        prefs.edit().putBoolean(prefs_timeout_active, state).apply()
    }

    fun eraseMasterPWD() {
        prefs.edit().remove(prefs_masterPWD).apply()
    }

    // Tiempo de inicio automatico (en milisegundos)
    private var SESSION_TIMEOUT = prefs.getInt(prefs_last_sessionTimeout, 5) * 60 * 1000

    /**
     * Establece el tiempo en minutos de la duracion del inicio de sesion automatico
     * @param minutes los minutos que se podra iniciar sin pedir contraseña
     */
    fun setSessionTimeout(minutes: Int) {
        prefs.edit().putInt("last_timeout", minutes).apply()
        SESSION_TIMEOUT = prefs.getInt("last_timeout", 5) * 60 * 1000
    }



    fun getTimeoutState(): Boolean {
        return prefs.getBoolean(prefs_timeout_active, true)
    }

    /**
     * Guarda la contraseña maestra en las preferencias
     * @param pwd La contraseña a guardar
     */
    fun saveMasterPWD(pwd:String){
        prefs.edit().putString(prefs_masterPWD, pwd).apply()
    }

    fun saveLoggedStatus(status:Boolean){
        prefs.edit().putBoolean(prefs_logged_status, status).apply()
        if (status) {
            // Guarda el tiempo actual en el que se inicio la sesion
            prefs.edit().putLong(prefs_session_start_time, System.currentTimeMillis()).apply()
        }
    }

    /**
     * Recupera la contraseña maestra de las preferencias
     * @return Contraseña maestra guardada en las preferencias
     */
    fun getMasterPWD():String{
        return prefs.getString(prefs_masterPWD, "")!!
    }

    fun getLoggedStatus():Boolean{
        return prefs.getBoolean(prefs_logged_status, false)
    }

    /**
     * Comprueba si el usuario abre la app dentro del tiempo de inicio automatico
     */
    fun isSessionActive(): Boolean {
        val sessionStartTime = prefs.getLong(prefs_session_start_time, 0)
        val currentTimeMillis = System.currentTimeMillis()
        val elapsedTime = currentTimeMillis - sessionStartTime
        return elapsedTime < SESSION_TIMEOUT
    }

    /**
     * Desactiva iniciar sesion automaticamente
     */
    fun clearSession() {
        prefs.edit().remove(prefs_session_start_time).apply()
        prefs.edit().remove(prefs_logged_status).apply()
    }
}