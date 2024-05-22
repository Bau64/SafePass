@file:Suppress("DEPRECATION")

package pfc.safepass.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import pfc.safepass.app.databinding.MainMenuBinding
import pfc.safepass.app.preferences.Preferences
import pfc.safepass.app.recycler.PassItem
import pfc.safepass.app.recycler.PasswordAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: MainMenuBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private lateinit var passwordAdapter: PasswordAdapter
    private lateinit var searchView: SearchView
    private lateinit var sortedList: List<PassItem>
    private lateinit var prefs: Preferences
    private var doubleBackPressed = false
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu?.findItem(R.id.menuItem_search)
        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            searchView.isIconifiedByDefault = false

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty())
                        passwordAdapter.filterByName(newText)
                    else
                        sortItems(true)
                    return true
                }
            })

            searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    searchView.requestFocus()
                    utils.showKeyboard(this@MainMenuActivity)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    utils.hideKeyboard(this@MainMenuActivity)
                    sortItems(true)
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
          R.id.menuItem_bin -> {
              gotoRecycleBin()
              return true
          }
          R.id.menuItem_sort -> {
              sortItems(false)
              return true
          }

          else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows an AlertDialog with options for sorting the password list
     */
    private fun sortItems(skipBuilder: Boolean) {
        sortedList = dataBaseHelper.getAllPassword(false)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        if (!skipBuilder) {
            val options = arrayOf(getString(R.string.sort_nickname_asc), getString(R.string.sort_nickname_desc), getString(R.string.sort_date_asc), getString(R.string.sort_date_desc))
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.menuItem_sort))

            builder.setSingleChoiceItems(options, prefs.getTempSort()) { dialog, option ->
                prefs.setTempSort(option)
                when (prefs.getTempSort()) {
                    1 -> passwordAdapter.refreshData(sortedList.sortedByDescending { it.nickname })
                    2 -> passwordAdapter.refreshData(sortedList.sortedByDescending { LocalDate.parse(it.date, formatter) })
                    3 -> passwordAdapter.refreshData(sortedList.sortedBy { LocalDate.parse(it.date, formatter) })
                    else -> passwordAdapter.refreshData(sortedList.sortedBy { it.nickname })
                }
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        } else {
            when (prefs.getTempSort()) {
                1 -> passwordAdapter.refreshData(sortedList.sortedByDescending { it.nickname })
                2 -> passwordAdapter.refreshData(sortedList.sortedByDescending { LocalDate.parse(it.date, formatter) })
                3 -> passwordAdapter.refreshData(sortedList.sortedBy { LocalDate.parse(it.date, formatter) })
                else -> passwordAdapter.refreshData(sortedList.sortedBy { it.nickname })
            }
        }
    }

    private fun goToLogin() {
        prefs.setTempSort(0)
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
    }

    private fun goToAddPassword(){
        startActivity(Intent(this, NewPasswordActivity::class.java))
        overridePendingTransition(R.anim.slide_bottom_2, R.anim.slide_top_2)
    }

    private fun goToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(R.anim.slide_left, R.anim.stay)
    }

    private fun gotoRecycleBin(){
        startActivity(Intent(this, RecycledItemsActivity::class.java))
        overridePendingTransition(R.anim.slide_right_2, R.anim.slide_right)
    }

    private fun initUI(){
        prefs = Preferences(applicationContext)
        dataBaseHelper = DataBaseHelper(this)
        passwordAdapter = PasswordAdapter(dataBaseHelper.getAllPassword(false), this, false)
        sortItems(true)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = passwordAdapter
        firstpwdHelper()
    }

    // Cuando el usuario presione atras se le pedira que lo vuelva a hacer para salir de la aplicacion
    @Deprecated("Deprecated")
    override fun onBackPressed() {
        if (doubleBackPressed) {
            super.onBackPressed()
            return
        }

        doubleBackPressed = true
        Toast.makeText(this, getString(R.string.toast_created_press_exit), Toast.LENGTH_SHORT).show()
        // If the users presses BACK twice in 2 seconds, the user exits the app
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackPressed = false }, 2000)
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.collapseActionView()
        sortItems(true)
        firstpwdHelper()
    }

    override fun onPause() {
        super.onPause()
        utils.hideKeyboard(this)
    }

    /**
     * Shows or hides the help message about how to create a password when the list is empty
     */
    private fun firstpwdHelper() {
        if (passwordAdapter.itemCount == 0)
            binding.menuNoPasswordHint.isVisible = true
        else
            binding.menuNoPasswordHint.isGone = true
    }
}