package pfc.safepass.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
//import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class PasswordAdapter(private var passList: List<Pass_Item>, context: Context, recycledList: Boolean) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {
    private val dataBaseHelper: DataBaseHelper = DataBaseHelper(context)
    private val recycledList = recycledList

    class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.item_service_image)
        val nickname: TextView = itemView.findViewById(R.id.lbl_item_service_name)
        val username: TextView = itemView.findViewById(R.id.lbl_item_service_user)
        val copyBtn: ImageButton = itemView.findViewById(R.id.copy_btn)
        val restoreBtn: ImageButton = itemView.findViewById(R.id.restore_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return PasswordViewHolder(view)
    }

    override fun getItemCount(): Int = passList.size

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val password = passList[position]

        val id = password.id
        holder.nickname.text = password.nickname
        holder.username.text = password.user

        if (byteArrayToBitmap(id) != null)
            holder.icon.setImageBitmap(byteArrayToBitmap(id))
        else
            holder.icon.setImageResource(R.drawable.icon_default_password)


        // Animacion de items del recyclerview
        holder.itemView.animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_anim)

        if (recycledList) {
            holder.copyBtn.setImageDrawable(getDrawable(holder.copyBtn.context, R.drawable.delete_forever_icon))
            holder.restoreBtn.isVisible = true
        }

        holder.restoreBtn.setOnClickListener {
            restorePassword(id, holder.restoreBtn.context)
        }

        holder.copyBtn.setOnClickListener {
            if (recycledList)
                delete(id, holder.copyBtn.context)
            else
                copyToClipboard(holder.copyBtn.context, password.password, true)
        }

        holder.copyBtn.setOnLongClickListener {
            if (!recycledList)
                password.user?.let { it1 -> copyToClipboard(holder.copyBtn.context, it1, false) }
            true
        }

        // Muestra un dialog preguntando al usuario si quiere editar o borrar la entrada
        holder.itemView.setOnLongClickListener {
            if (!recycledList) {
                val context = holder.itemView.context
                val builder = AlertDialog.Builder(context)
                builder.setTitle(context.getString(R.string.options))
                val opciones = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))

                builder.setItems(opciones) { _, opciones ->
                    when (opciones) {
                        0 -> {update(context, id)}
                        1 -> {delete(id, context); Toast.makeText(context, context.getString(R.string.toast_deleted_password), Toast.LENGTH_SHORT).show()}
                    }
                }
                val dialog = builder.create()
                dialog.show()
            }
            true
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val actividad = context as Activity
            startActivity(context, Intent(context, ItemDetailsActivity::class.java).apply { putExtra("id", id) }, null)
            actividad.overridePendingTransition(R.anim.zoom_in, R.anim.stay)
        }
    }

    /**
     * Elimina una entrada
     * @param id id de la entrada
     */
    private fun delete(id: Int, context: Context){
        if (recycledList) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.dialog_erasePWDforever_title))
            builder.setMessage(context.getString(R.string.dialog_erasePWDforever_message))

            builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                dataBaseHelper.deletePassword(id)
                refreshData(dataBaseHelper.getAllPassword(true))
                dialog.dismiss()
            }

            builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
        else {
            dataBaseHelper.recyclePassword(id)
            refreshData(dataBaseHelper.getAllPassword(false))
        }
    }

    /**
     * Actualiza una entrada
     * @param context
     * @param id id de la entrada
     */
    private fun update(context: Context, id: Int){
        val activity = context as Activity
        startActivity(context, Intent(context, New_Password_Activity::class.java).apply { putExtra("id", id) }, null)
        activity.overridePendingTransition(R.anim.slide_bottom_2, R.anim.slide_top_2)
    }

    /**
     * Refresca la lista
     * @param newPassList Lista de contraseñas
     */
    fun refreshData(newPassList: List<Pass_Item>) {
        passList = newPassList
        notifyDataSetChanged()
    }

    fun restorePassword(id: Int, context: Context) {
        dataBaseHelper.restorePassword(id)
        Toast.makeText(context, "Contraseña restaurada", Toast.LENGTH_SHORT).show()
        refreshData(dataBaseHelper.getAllPassword(true))
    }

    /**
     * Convierte byteArray de imagen en base de datos a bitmap para verse en la imagen de la entrada
     * @return Bitmap
     */
    fun byteArrayToBitmap(id: Int): Bitmap? {
        val item = dataBaseHelper.getPasswordbyID(id)
        if (item?.icon == null)
            return null
        else
            return BitmapFactory.decodeByteArray(item.icon, 0, item.icon!!.size)
    }

    fun filterByName(text: String) {
        val filteredList = mutableListOf<Pass_Item>()

        for (item in passList) {
            if (item.nickname.contains(text, true))
                filteredList.add(item)
        }
        passList = filteredList
        notifyDataSetChanged()
    }

    /**
     * Copia la contraseña del item al portapapeles
     * @param context Contexto actual
     * @param texto Contraseña a copiar
     */
    private fun copyToClipboard(context: Context, texto: String?, isPassword: Boolean){
        if (texto.isNullOrEmpty())
            Toast.makeText(context, context.getString(R.string.toast_error_noUser), Toast.LENGTH_SHORT).show()
        else {
            val clipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("pwd", texto)
            clipboardManager.setPrimaryClip(clipData)
            if (isPassword)
                Toast.makeText(context, context.getString(R.string.toast_copiedPassword), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(context, context.getString(R.string.toast_copiedUser), Toast.LENGTH_SHORT).show()
        }
    }
}