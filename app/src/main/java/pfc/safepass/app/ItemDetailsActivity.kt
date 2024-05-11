package pfc.safepass.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pfc.safepass.app.databinding.ActivityItemDetailsBinding
import pfc.safepass.app.recycler.PassItem

class ItemDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailsBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        dataBaseHelper = DataBaseHelper(this)
        initUI()
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuItem_edit -> {
                goToUpdatePassword()
                return true
            }
            R.id.menuItem_detailsBin -> {
                deletePassword()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initUI() {
        val itemId = intent.getIntExtra("id", -1)
        val item = dataBaseHelper.getPasswordbyID(itemId)!! // Obtener item
        fillFields(item)

        binding.detailCopyPassword.setOnClickListener {
            utils.copyToClipboard(this, item.password, true)

            // Cambiar el icono y esperar 1 segundo para volverlo a cambiar
            binding.detailCopyPassword.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.check_done_icon))
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.detailCopyPassword.setImageDrawable(AppCompatResources.getDrawable(this@ItemDetailsActivity, R.drawable.item_copy_dark))
                }
            }
        }

        binding.detailCopyUser.setOnClickListener {
            utils.copyToClipboard(this, item.user, false)


            binding.detailCopyUser.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.check_done_icon))
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.detailCopyUser.setImageDrawable(AppCompatResources.getDrawable(this@ItemDetailsActivity, R.drawable.item_copy_dark))
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.stay, R.anim.zoom_out)
    }

    private fun byteArrayToBitmap(id: Int): Bitmap {
        val item = dataBaseHelper.getPasswordbyID(id)
        return BitmapFactory.decodeByteArray(item!!.icon, 0, item.icon!!.size)
    }

    private fun goToUpdatePassword() {
        startActivity(Intent(this, NewPasswordActivity::class.java).apply { putExtra("id", intent.getIntExtra("id", -1)) }, null)
        overridePendingTransition(R.anim.slide_bottom_2, R.anim.slide_top_2)
    }

    private fun deletePassword() {
        super.onBackPressed()
        dataBaseHelper.recyclePassword(intent.getIntExtra("id", -1))
        Toast.makeText(this, getString(R.string.toast_deleted_password), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        fillFields(dataBaseHelper.getPasswordbyID(intent.getIntExtra("id", -1))!!)
        super.onResume()
    }

    private fun fillFields(item: PassItem) {
        binding.toolbar2.title = item.nickname
        binding.passwordInput.setText(item.password)
        binding.creationdateInput.setText(item.date)

        if (item.icon == null) {
            binding.passwordIcon.setImageResource(R.drawable.icon_default_password)
        } else {
            binding.passwordIcon.setImageBitmap(byteArrayToBitmap(item.id))
        }

        if (!item.user.isNullOrEmpty()) {
            binding.userInputLayout.isVisible = true
            binding.userInput.setText(item.user)
        } else {
            binding.userInputLayout.isGone = true
            binding.detailCopyUser.isGone = true
        }

        if (!item.notes.isNullOrEmpty()) {
            binding.notesInputLayout.isVisible = true
            binding.notesInput.setText(item.notes)
        } else {
            binding.notesInputLayout.isGone = true
        }
    }
}