@file:Suppress("DEPRECATION")

package pfc.safepass.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import pfc.safepass.app.databinding.SettingsActivityBinding
import pfc.safepass.app.preferences.Preferences

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_right)
    }

    override fun onSupportNavigateUp(): Boolean {
        overridePendingTransition(R.anim.stay, R.anim.slide_right)
        super.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val prefs = Preferences(requireContext())

            val automaticLoginSwitch = findPreference<SwitchPreferenceCompat>("automaticLogin")
            automaticLoginSwitch?.isChecked = prefs.getTimeoutState()
            automaticLoginSwitch?.setOnPreferenceChangeListener { _, newValue ->
                val switch = newValue as Boolean
                if (!switch) { // Automatic login disabled
                    prefs.setTimeoutState(false)
                    prefs.clearSession()
                } else {
                    prefs.setTimeoutState(true)
                    prefs.setSessionTimeout(prefs.getLastSessionTimeout())
                }
                true
            }

            val automaticLoginMinutes = findPreference<ListPreference>("automaticLogin_time")
            automaticLoginMinutes?.setOnPreferenceChangeListener { _, newValue ->
                val selectedValue = newValue as String
                prefs.setSessionTimeout(selectedValue.toInt())
                true
            }

            val appThemePreference = findPreference<ListPreference>("appTheme")
            appThemePreference?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as String
                prefs.setAppTheme(value)
                AppCompatDelegate.setDefaultNightMode(prefs.getAppTheme())
                true
            }

            val changemasterpwdPreference = findPreference<Preference>("change_masterPWD")
            changemasterpwdPreference?.setOnPreferenceClickListener {
                val context = requireContext()
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(context.getString(R.string.dialog_resetMasterPWD_title))
                builder.setMessage(context.getString(R.string.dialog_resetMasterPWD_message))

                builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                    prefs.eraseMasterPWD()
                    dialog.dismiss()
                    requireActivity().apply {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finishAffinity()
                    }
                }

                builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
                true
            }
        }
    }
}