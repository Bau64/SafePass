package pfc.safepass.app

import android.content.Intent
//import android.hardware.biometrics.BiometricManager
//import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
//import pfc.safepass.app.SafePassApp.Companion.prefs
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import pfc.safepass.app.databinding.LoginScreenBinding
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
     * Verifica si el sistema puede acceder a huellas dactilares
     * @return Boolean
     */
    private fun checkBiometricHW():Boolean{
        var supportedHW = false
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS -> supportedHW = true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> supportedHW = false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> supportedHW = false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> supportedHW = false
        }
        return supportedHW
    }

    private fun initUI(){
        val prefs = Preferences(applicationContext)
        binding.btnLogin.setOnClickListener {
            val user_pwd = binding.passwordText.text.toString()
            if (user_pwd == prefs.getMasterPWD()) {
                goToMainMenu()
                finish()
            } else {
                binding.newPasswordPasswordLayout.error = applicationContext.getString(R.string.masterPWD_error)
            }
        }

        // Si el sistema puede usar huellas dactilares se activara el metodo de acceder con biometria
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
    }
}