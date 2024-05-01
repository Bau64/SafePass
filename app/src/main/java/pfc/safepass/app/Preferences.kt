package pfc.safepass.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys

class Preferences(context: Context) {
    private val PREFS_NAME = "pfc.safepass.mypreferences"
    private val prefs_masterPWD = "masterPWD"
    private val prefs_logged_status = "loggedStatus"
    private val prefs_session_start_time = "sessionStartTime"

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

    // Tiempo de inicio automatico (primer numero en minutos)
    private val SESSION_TIMEOUT = 1 * 60 * 1000

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