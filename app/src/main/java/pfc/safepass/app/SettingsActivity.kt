package pfc.safepass.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import pfc.safepass.app.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val prefs = Preferences(requireContext())

            val automatic_login_switch = findPreference<SwitchPreferenceCompat>("automaticLogin")
            //automatic_login_switch?.isChecked = prefs.getSessionTimeout() > 0
            automatic_login_switch?.isChecked = prefs.getTimeoutState()
            automatic_login_switch?.setOnPreferenceChangeListener { _, newValue ->
                var switch = newValue as Boolean
                if (!switch) { // inicio automatico desactivado
                    prefs.setTimeoutState(false)
                    prefs.clearSession()
                } else {
                    prefs.setTimeoutState(true)
                    prefs.setSessionTimeout(prefs.getLastSessionTimeout())
                }
                true
            }

            val automatic_login_minutes = findPreference<ListPreference>("automaticLogin_time")
            automatic_login_minutes?.setOnPreferenceChangeListener { preference, newValue ->
                val selectedValue = newValue as String
                prefs.setSessionTimeout(selectedValue.toInt())
                true
            }

            val changeMasterPWD_preference = findPreference<Preference>("change_masterPWD")
            changeMasterPWD_preference?.setOnPreferenceChangeListener { preference, newValue ->

                true
            }
        }
    }
}