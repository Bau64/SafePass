package pfc.safepass.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import pfc.safepass.app.databinding.ActivityRecycledItemsBinding
import pfc.safepass.app.recycler.PasswordAdapter

class RecycledItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecycledItemsBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private lateinit var passwordAdapter: PasswordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycledItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar3)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recycler, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuItem_deleteAllPasswords -> {
                deleteAllPasswords()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initUI() {
        dataBaseHelper = DataBaseHelper(this)
        passwordAdapter = PasswordAdapter(dataBaseHelper.getAllPassword(true), this, true)
        binding.recyclerview2.layoutManager = LinearLayoutManager(this)
        binding.recyclerview2.adapter = passwordAdapter
        recycler_helper()
    }

    private fun deleteAllPasswords(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_eraseAllPWDforever_title))
        builder.setMessage(getString(R.string.dialog_eraseAllPWDforever_message))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dataBaseHelper.deleteAllPasswords()
            passwordAdapter.refreshData(dataBaseHelper.getAllPassword(true))
            recycler_helper()
            Toast.makeText(this, getString(R.string.toast_emptyBin), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun recycler_helper() {
        if (passwordAdapter.itemCount == 0)
            binding.recycledHint.isVisible = true
        else
            binding.recycledHint.isGone = true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_left, R.anim.slide_left_3)
    }

    override fun onSupportNavigateUp(): Boolean {
        overridePendingTransition(R.anim.slide_left, R.anim.slide_left_3)
        super.onBackPressed()
        return true
    }
}