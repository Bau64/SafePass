package pfc.safepass.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pfc.safepass.app.databinding.ActivityItemDetailsBinding

class ItemDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailsBinding
    private lateinit var dataBaseHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataBaseHelper = DataBaseHelper(this)
        initUI()
    }

    fun initUI() {
        binding.toolbar2.setNavigationOnClickListener {
            finish()
        }

        val item_id = intent.getIntExtra("id", -1)
        val item = dataBaseHelper.getPasswordbyID(item_id)!! // Obtener item

        binding.toolbar2.title = item.nickname
        binding.passwordInput.setText(item.password)
        binding.creationdateInput.setText(item.date)

        binding.detailCopyPassword.setOnClickListener {
            copyToClipboard(this, item.password, true)

            // Cambiar el icono y esperar 1 segundo para volverlo a cambiar
            binding.detailCopyPassword.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.check_done_icon))
            //binding.detailCopyPassword.setImageDrawable(getDrawable(R.drawable.check_done_icon))
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.detailCopyPassword.setImageDrawable(AppCompatResources.getDrawable(this@ItemDetailsActivity, R.drawable.item_copy_light))
                }
            }
        }

        binding.detailCopyUser.setOnClickListener {
            copyToClipboard(this, item.user, false)

            binding.detailCopyUser.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.check_done_icon))
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.detailCopyUser.setImageDrawable(AppCompatResources.getDrawable(this@ItemDetailsActivity, R.drawable.item_copy_light))
                }
            }
        }

        if (item.icon == null) {
            binding.passwordIcon.setImageResource(R.drawable.icon_default_password)
        } else {
            binding.passwordIcon.setImageBitmap(byteArrayToBitmap(item.id))
        }

        if (!item.user.isNullOrEmpty()) {
            binding.userInput.setText(item.user)
        } else {
            binding.userInputLayout.isGone = true
            binding.detailCopyUser.isGone = true
        }

        if (!item.notes.isNullOrEmpty()) {
            binding.notesInput.setText(item.notes)
        } else {
            binding.notesInputLayout.isGone = true
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

    private fun copyToClipboard(context: Context, texto: String?, isPassword: Boolean){
        if (texto.isNullOrEmpty())
            Toast.makeText(context, context.getString(R.string.toast_error_noUser), Toast.LENGTH_SHORT).show()
        else {
            val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("pwd", texto)
            clipboardManager.setPrimaryClip(clipData)
            if (isPassword)
                Toast.makeText(context, context.getString(R.string.toast_copiedPassword), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(context, context.getString(R.string.toast_copiedUser), Toast.LENGTH_SHORT).show()
        }
    }
}