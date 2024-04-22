package pfc.safepass.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class Preferences(context: Context) {
    private val PREFS_NAME = "pfc.bautistaczupil.mypreferences"
    private val prefs_masterPWD = "masterPWD"
    private val prefs_logged_status = "loggedStatus"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

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