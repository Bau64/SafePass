package pfc.safepass.app.preferences

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

    // Encrypt preferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Checks the user value from settings screen for automatic login time frame
     */
    fun getLastSessionTimeout(): Int {
        return prefs.getInt("last_timeout", 5)
    }

    /**
     * Sets a new time frame for automatic login when the user changes it from the settings screen
     */
    fun setTimeoutState(state: Boolean) {
        prefs.edit().putBoolean(prefs_timeout_active, state).apply()
    }

    /**
     * Removes master password from preferences
     */
    fun eraseMasterPWD() {
        prefs.edit().remove(prefs_masterPWD).apply()
    }

    // Automatic login time frame (in miliseconds)
    private var SESSION_TIMEOUT = prefs.getInt(prefs_last_sessionTimeout, 5) * 60 * 1000

    /**
     * Establishes automatic login time frame in minutes
     * @param minutes Automatic login frame in minutes
     */
    fun setSessionTimeout(minutes: Int) {
        prefs.edit().putInt("last_timeout", minutes).apply()
        SESSION_TIMEOUT = prefs.getInt("last_timeout", 5) * 60 * 1000
    }

    /**
     * Checks if the automatic login is activated or not
     */
    fun getTimeoutState(): Boolean {
        return prefs.getBoolean(prefs_timeout_active, true)
    }

    /**
     * Saves master password in preferences
     * @param pwd Master password to save
     */
    fun saveMasterPWD(pwd:String){
        prefs.edit().putString(prefs_masterPWD, pwd).apply()
    }

    /**
     * Saves the time the user has logged in
     * @param status Logged status
     */
    fun saveLoggedStatus(status:Boolean){
        prefs.edit().putBoolean(prefs_logged_status, status).apply()
        if (status) {
            // Saves current time
            prefs.edit().putLong(prefs_session_start_time, System.currentTimeMillis()).apply()
        }
    }

    /**
     * Returns master password from preferences
     * @return Master password
     */
    fun getMasterPWD():String{
        return prefs.getString(prefs_masterPWD, "")!!
    }

    fun getLoggedStatus():Boolean{
        return prefs.getBoolean(prefs_logged_status, false)
    }

    /**
     * Checks if the user opened the app in the established automatic login time frame
     */
    fun isSessionActive(): Boolean {
        val sessionStartTime = prefs.getLong(prefs_session_start_time, 0)
        val currentTimeMillis = System.currentTimeMillis()
        val elapsedTime = currentTimeMillis - sessionStartTime
        return elapsedTime < SESSION_TIMEOUT
    }

    /**
     * Deactivates automatic login
     */
    fun clearSession() {
        prefs.edit().remove(prefs_session_start_time).apply()
        prefs.edit().remove(prefs_logged_status).apply()
    }
}