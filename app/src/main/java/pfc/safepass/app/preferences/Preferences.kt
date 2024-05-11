package pfc.safepass.app.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Preferences(context: Context) {
    private val prefsName = "pfc.safepass.mypreferences"
    private val prefsMasterpwd = "masterPWD"
    private val prefsLoggedStatus = "loggedStatus"
    private val prefsSessionStartTime = "sessionStartTime"
    private val prefsLastSessiontimeout = "last_timeout"
    private var prefsTimeoutActive = "timeout_active"
    private var prefsAppTheme = "app_theme"

    // Encrypt preferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        prefsName,
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
        prefs.edit().putBoolean(prefsTimeoutActive, state).apply()
    }

    /**
     * Removes master password from preferences
     */
    fun eraseMasterPWD() {
        prefs.edit().remove(prefsMasterpwd).apply()
    }

    // Automatic login time frame (in miliseconds)
    private var sessionTimeout = prefs.getInt(prefsLastSessiontimeout, 5) * 60 * 1000

    /**
     * Establishes automatic login time frame in minutes
     * @param minutes Automatic login frame in minutes
     */
    fun setSessionTimeout(minutes: Int) {
        prefs.edit().putInt("last_timeout", minutes).apply()
        sessionTimeout = prefs.getInt("last_timeout", 5) * 60 * 1000
    }

    /**
     * Checks if the automatic login is activated or not
     */
    fun getTimeoutState(): Boolean {
        return prefs.getBoolean(prefsTimeoutActive, true)
    }

    /**
     * Saves master password in preferences
     * @param pwd Master password to save
     */
    fun saveMasterPWD(pwd:String){
        prefs.edit().putString(prefsMasterpwd, pwd).apply()
    }

    /**
     * Saves the time the user has logged in
     * @param status Logged status
     */
    fun saveLoggedStatus(status:Boolean){
        prefs.edit().putBoolean(prefsLoggedStatus, status).apply()
        if (status) {
            // Saves current time
            prefs.edit().putLong(prefsSessionStartTime, System.currentTimeMillis()).apply()
        }
    }

    /**
     * Returns master password from preferences
     * @return Master password
     */
    fun getMasterPWD():String{
        return prefs.getString(prefsMasterpwd, "")!!
    }

    fun getLoggedStatus():Boolean{
        return prefs.getBoolean(prefsLoggedStatus, false)
    }

    /**
     * Checks if the user opened the app in the established automatic login time frame
     */
    fun isSessionActive(): Boolean {
        val sessionStartTime = prefs.getLong(prefsSessionStartTime, 0)
        val currentTimeMillis = System.currentTimeMillis()
        val elapsedTime = currentTimeMillis - sessionStartTime
        return elapsedTime < sessionTimeout
    }

    /**
     * Deactivates automatic login
     */
    fun clearSession() {
        prefs.edit().remove(prefsSessionStartTime).apply()
        prefs.edit().remove(prefsLoggedStatus).apply()
    }

    fun setAppTheme(value: String) {
        var themeInt = -1

        if (value == "Light")
            themeInt = AppCompatDelegate.MODE_NIGHT_NO
        else if (value == "Dark")
            themeInt = AppCompatDelegate.MODE_NIGHT_YES

        prefs.edit().putInt("app_theme", themeInt).apply()
    }

    fun getAppTheme(): Int {
        return prefs.getInt(prefsAppTheme, -1)
    }
}