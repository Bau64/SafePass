package pfc.safepass.app
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
<<<<<<< Updated upstream
//import pfc.safepass.app.DataBaseHelper
=======
>>>>>>> Stashed changes

class PasswordAdapter(private var passList: List<Pass_Item>, context: Context) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    private val dataBaseHelper: DataBaseHelper = DataBaseHelper(context)

    class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.item_service_image)
        val nickname: TextView = itemView.findViewById(R.id.lbl_item_service_name)
        val username: TextView = itemView.findViewById(R.id.lbl_item_service_user)
        val copyBtn: ImageButton = itemView.findViewById(R.id.copy_btn)
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
<<<<<<< Updated upstream
        holder.icon.setImageBitmap(byteArrayToBitmap(id))
=======
        if (byteArrayToBitmap(id) != null)
            holder.icon.setImageBitmap(byteArrayToBitmap(id))
        else
            holder.icon.setImageResource(R.drawable.icon_default_password)
>>>>>>> Stashed changes
        val pwd = password.password
        //val link = password.link

        // Animacion de items del recyclerview
        holder.itemView.animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_anim)

        holder.copyBtn.setOnClickListener {
            copyToClipboard(holder.copyBtn.context, pwd)
        }

        // Muestra un dialog preguntando al usuario si quiere editar o borrar la entrada
        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            val builder = AlertDialog.Builder(context)
            //builder.setTitle(Resources.getSystem().getString(R.string.options))
            builder.setTitle("Opciones")
            //val opciones = arrayOf(Resources.getSystem().getString(R.string.edit), Resources.getSystem().getString(R.string.delete)) // Opciones del dialog (Editar, Borrar)
            val opciones = arrayOf("Editar", "Borrar")
            builder.setItems(opciones) { _, opciones ->
                when (opciones) {
                    0 -> {update(context, id)}
<<<<<<< Updated upstream
                    1 -> {delete(id); Toast.makeText(context, Resources.getSystem().getString(R.string.toolbar_edit_password), Toast.LENGTH_SHORT).show()}
=======
                    //1 -> {delete(id); Toast.makeText(context, Resources.getSystem().getString(R.string.toolbar_edit_password), Toast.LENGTH_SHORT).show()}
                    1 -> {delete(id); Toast.makeText(context, "Contraseña eliminada", Toast.LENGTH_SHORT).show()}
>>>>>>> Stashed changes
                }
            }
            val dialog = builder.create()
            dialog.show()
            true
        }
    }

    /**
     * Elimina una entrada
     * @param id id de la entrada
     */
    private fun delete(id: Int){
        dataBaseHelper.deletePassword(id)
        refreshData(dataBaseHelper.getAllPassword())
    }

    /**
     * Actualiza una entrada
     * @param context
     * @param id id de la entrada
     */
    private fun update(context: Context, id: Int){
        startActivity(context, Intent(context, New_Password_Activity::class.java).apply { putExtra("id", id) }, null)
    }

    /**
     * Refresca la lista
     * @param newPassList Lista de contraseñas
     */
    fun refreshData(newPassList: List<Pass_Item>) {
        passList = newPassList
        notifyDataSetChanged()
    }

    /**
     * Convierte byteArray de imagen en base de datos a bitmap para verse en la imagen de la entrada
     * @return Bitmap
     */
<<<<<<< Updated upstream
    fun byteArrayToBitmap(id: Int): Bitmap {
        val item = dataBaseHelper.getPasswordbyID(id)
        return BitmapFactory.decodeByteArray(item!!.icon, 0, item.icon.size)
=======
    fun byteArrayToBitmap(id: Int): Bitmap? {
        val item = dataBaseHelper.getPasswordbyID(id)
        if (item!!.icon == null)
            return null
        else
            return BitmapFactory.decodeByteArray(item.icon, 0, item.icon!!.size)
>>>>>>> Stashed changes
    }

    /**
     * Copia la contraseña del item al portapapeles
     * @param context Contexto actual
     * @param texto Contraseña a copiar
     */
    private fun copyToClipboard(context: Context, texto: String){
        val clipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("pwd", texto)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "copiada", Toast.LENGTH_SHORT).show()
    }
}