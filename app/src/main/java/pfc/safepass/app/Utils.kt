package pfc.safepass.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pfc.safepass.app.recycler.Pass_Item
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Utils {

    /**
     * Converts a password object's ByteArray to Bitmap
     * @param passItem Password object
     * @return Bitmap or null
     */
    fun byteArrayToBitmap(passItem: Pass_Item): Bitmap? {
        return if (passItem.icon == null)
            null
        else
            BitmapFactory.decodeByteArray(passItem.icon, 0, passItem.icon!!.size)
    }

    /**
     * Converts a bitmap to a ByteArray
     * @param img Bitmap
     */
    fun imgtoByteArray(img: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Copies a password or username to the clipboard
     * @param context
     * @param text Password or Username
     * @param isPassword
     */
    fun copyToClipboard(context: Context, text: String?, isPassword: Boolean){
        if (text.isNullOrEmpty())
            Toast.makeText(context, context.getString(R.string.toast_error_noUser), Toast.LENGTH_SHORT).show()
        else {
            val clipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("pwd", text)
            clipboardManager.setPrimaryClip(clipData)
            if (isPassword)
                Toast.makeText(context, context.getString(R.string.toast_copiedPassword), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(context, context.getString(R.string.toast_copiedUser), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Returns the current day in dd/mm/yyyy format
     * @return Current date
     */
    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }
}