package pfc.safepass.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
//import pfc.safepass.app.SafePassApp.Companion.prefs
//import pfc.bautistaczupil.safepass.databinding.ActivityMainBinding
import pfc.safepass.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        val prefs = Preferences(applicationContext)
        // Si hay una clave maestra creada previamente se ira directo a la pantalla de login
        if (prefs.getMasterPWD().isNotEmpty()){
            goToLogin()
            finish()
        } else {
            binding.newPassword.doOnTextChanged{ _, _, _, _ ->
                matchPasswords()
            }

            binding.newPasswordConfirm.doOnTextChanged{ _, _, _, _ ->
                matchPasswords()
            }

            binding.btnSaveMasterPWD.setOnClickListener {
                prefs.saveMasterPWD(binding.newPasswordConfirm.text.toString()) //Guarda la contraseña maestra
                Toast.makeText(this, getString(R.string.masterPWD_created), Toast.LENGTH_SHORT).show()
                goToMainMenu()
                finish()
            }
        }
    }

    /**
     * Verifica que las contraseñas coincidan
     */
    private fun matchPasswords(){
        val newPassword = binding.newPassword.text.toString()
        val newPasswordConfirm = binding.newPasswordConfirm.text.toString()

        if ((newPassword == newPasswordConfirm)) {
            if (newPassword.isEmpty() && newPasswordConfirm.isEmpty()) {
                binding.btnSaveMasterPWD.isEnabled = false
            } else {
                binding.confirmPasswordInputlayout.error = null
                binding.btnSaveMasterPWD.isEnabled = true
            }
        } else {
            binding.confirmPasswordInputlayout.error = getString(R.string.matching_passwords_error)
            binding.btnSaveMasterPWD.isEnabled = false
        }
    }

    private fun goToMainMenu(){
        startActivity(Intent(this, MainMenuActivity::class.java))
    }

    private fun goToLogin(){
        startActivity(Intent(this, LoginActivity::class.java))
    }
}