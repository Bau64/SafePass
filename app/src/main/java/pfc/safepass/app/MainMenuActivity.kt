package pfc.safepass.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import pfc.safepass.app.databinding.MainMenuBinding

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: MainMenuBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private lateinit var passwordAdapter: PasswordAdapter
    private var doubleBackPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)

        val searchItem = menu?.findItem(R.id.menuItem_search)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty())
                        passwordAdapter.filterByName(newText)
                    else
                        passwordAdapter.refreshData(dataBaseHelper.getAllPassword())
                    return true
                }

            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
          R.id.menuItem_addNew -> {
              goToAddPassword()
              return true
          }
          R.id.menuItem_lock -> {
              Preferences(applicationContext).clearSession()
              goToLogin()
              Toast.makeText(this, getString(R.string.database_locked), Toast.LENGTH_SHORT).show()
              finish()
              return true
          }
          R.id.menuItem_settings -> {
              goToSettings()
              return true
          }

          else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
    }

    private fun goToAddPassword(){
        startActivity(Intent(this, New_Password_Activity::class.java))
        overridePendingTransition(R.anim.slide_bottom_2, R.anim.slide_top_2)
    }

    private fun goToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun initUI(){
        dataBaseHelper = DataBaseHelper(this)
        passwordAdapter = PasswordAdapter(dataBaseHelper.getAllPassword(), this)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = passwordAdapter
        firstPWD_helper()
    }

    // Cuando el usuario presione atras se le pedira que lo vuelva a hacer para salir de la aplicacion
    override fun onBackPressed() {
        if (doubleBackPressed) {
            super.onBackPressed()
            return
        }

        doubleBackPressed = true
        Toast.makeText(this, getString(R.string.toast_created_press_exit), Toast.LENGTH_SHORT).show()
        // Si el usuario presiona ATRAS dentro de 2 segundos, saldra de la aplicación
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackPressed = false }, 2000)
    }

    override fun onResume() {
        super.onResume()
        firstPWD_helper()
        //if (dataBaseHelper.getPasswordCount() != passwordAdapter.itemCount)
        passwordAdapter.refreshData(dataBaseHelper.getAllPassword())
    }

    /**
     * Activa o desactiva el mensaje que indica como crear una contraseña en caso de que no haya ninguna contraseña creada
     */
    private fun firstPWD_helper() {
        if (dataBaseHelper.getPasswordCount() == 0)
            binding.menuNoPasswordHint.isVisible = true
        else
            binding.menuNoPasswordHint.isGone = true
    }
}