package pfc.safepass.app

import android.content.Intent
//import android.hardware.biometrics.BiometricManager
//import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
//import pfc.safepass.app.preferences.SafePassApp.Companion.prefs
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import pfc.safepass.app.databinding.LoginScreenBinding
import pfc.safepass.app.preferences.Preferences
//import pfc.bautistaczupil.safepass.databinding.LoginScreenBinding
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var binding: LoginScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        executor = ContextCompat.getMainExecutor(this)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Checks if the sistem can access biometrics
     */
    private fun checkBiometricHW():Boolean{
        var supportedHW = false
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun initUI(){
        val prefs = Preferences(applicationContext)
        binding.btnLogin.setOnClickListener {
            val userPwd = binding.passwordText.text.toString()
            if (userPwd == prefs.getMasterPWD()) {
                prefs.saveLoggedStatus(true)
                goToMainMenu()
                finish()
            } else {
                binding.newPasswordPasswordLayout.error = applicationContext.getString(R.string.masterPWD_error)
            }
        }

        // If the system can use biometrics it will activate the biometric login option
        if (checkBiometricHW()) {
            binding.biometricLink.isVisible = true
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        goToMainMenu()
                        finish()
                    }
                })

            val promptInfo = PromptInfo.Builder()
                .setTitle(applicationContext.getString(R.string.biometric_title))
                .setSubtitle(applicationContext.getString(R.string.biometric_subtitle))
                .setNegativeButtonText(applicationContext.getString(R.string.cancel))
                .build()

            binding.biometricLink.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            binding.biometricLink.isVisible = false
        }
    }

    fun goToMainMenu(){
        startActivity(Intent(this, MainMenuActivity::class.java))
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
    }
}