package pfc.safepass.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
//import pfc.bautistaczupil.safepass.databinding.MainMenuBinding
import pfc.safepass.app.databinding.MainMenuBinding

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: MainMenuBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private lateinit var passwordAdapter: PasswordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
          R.id.menuItem_addNew -> {
              goToAddPassword()
              return true
          }
          R.id.menuItem_lock -> {
              goToLogin()
              Toast.makeText(this, getString(R.string.database_locked), Toast.LENGTH_SHORT).show()
              finish()
              return true
          }

          R.id.menuItem_search -> {
              // Iniciar busqueda de items por texto
              TODO("Implementar busqueda")
              return true
          }
          else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun goToAddPassword(){
        startActivity(Intent(this, New_Password_Activity::class.java))
    }

    private fun initUI(){
        dataBaseHelper = DataBaseHelper(this)
        passwordAdapter = PasswordAdapter(dataBaseHelper.getAllPassword(), this)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = passwordAdapter

        // Si no hay contraseñas se mostrara un mensaje de como crear una
        if (passwordAdapter.itemCount == 0)
            binding.menuNoPasswordHint.isVisible = true
        else
            binding.menuNoPasswordHint.isGone = true
    }

    override fun onResume() {
        super.onResume()
<<<<<<< Updated upstream
        if (dataBaseHelper.getPasswordCount() != passwordAdapter.itemCount)
=======
        //if (dataBaseHelper.getPasswordCount() != passwordAdapter.itemCount)
>>>>>>> Stashed changes
            passwordAdapter.refreshData(dataBaseHelper.getAllPassword())
    }

    fun refreshData(){
        passwordAdapter.refreshData(dataBaseHelper.getAllPassword())
    }
}