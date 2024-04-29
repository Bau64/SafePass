package pfc.safepass.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys

class Preferences(context: Context) {
    private val PREFS_NAME = "pfc.bautistaczupil.mypreferences"
    private val prefs_masterPWD = "masterPWD"
    private val prefs_logged_status = "loggedStatus"
    //private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveMasterPWD(pwd:String){
        prefs.edit().putString(prefs_masterPWD, pwd).apply()
    }

    fun saveLoggedStatus(status:Boolean){
        prefs.edit().putBoolean(prefs_logged_status, status).apply()
    }

    fun getMasterPWD():String{
        return prefs.getString(prefs_masterPWD, "")!!
    }

    fun getLoggedStatus():Boolean{
        return prefs.getBoolean(prefs_logged_status, false)
    }
}