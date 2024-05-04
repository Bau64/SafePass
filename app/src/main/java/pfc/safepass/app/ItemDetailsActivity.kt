package pfc.safepass.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import pfc.safepass.app.databinding.ActivityItemDetailsBinding
import java.io.ByteArrayOutputStream

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
}