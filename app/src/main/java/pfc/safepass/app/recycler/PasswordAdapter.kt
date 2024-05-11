package pfc.safepass.app.recycler

import android.app.Activity
import android.content.Context
import android.content.Intent
//import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pfc.safepass.app.DataBaseHelper
import pfc.safepass.app.ItemDetailsActivity
import pfc.safepass.app.NewPasswordActivity
import pfc.safepass.app.R
import pfc.safepass.app.Utils

class PasswordAdapter(private var passList: List<Pass_Item>, context: Context, private val recycledList: Boolean) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {
    private val dataBaseHelper: DataBaseHelper = DataBaseHelper(context)
    private val utils = Utils()

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
        val context = holder.copyBtn.context

        val id = password.id
        holder.nickname.text = password.nickname
        holder.username.text = password.user

        val passwordBitmap = utils.byteArrayToBitmap(dataBaseHelper.getPasswordbyID(id)!!)
        if (passwordBitmap != null)
            holder.icon.setImageBitmap(passwordBitmap)
        else
            holder.icon.setImageResource(R.drawable.icon_default_password)


        // Recyclerview animations
        holder.itemView.animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_anim)

        if (recycledList) {
            holder.copyBtn.setImageDrawable(getDrawable(context,
                R.drawable.delete_forever_icon
            ))
            holder.restoreBtn.isVisible = true
        }

        holder.restoreBtn.setOnClickListener {
            dataBaseHelper.restorePassword(id)
            Toast.makeText(context, context.getString(R.string.toast_restored_password), Toast.LENGTH_SHORT).show()
            refreshData(dataBaseHelper.getAllPassword(true))
        }

        holder.copyBtn.setOnClickListener {
            if (recycledList)
                delete(id, context)
            else {
                utils.copyToClipboard(context, password.password, true)

                // Change icon and wait 1 second to change it back
                holder.copyBtn.setImageDrawable(getDrawable(context,
                    R.drawable.check_done_icon
                ))
                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        holder.copyBtn.setImageDrawable(getDrawable(context,
                            R.drawable.item_copy_light
                        ))
                    }
                }
            }
        }

        holder.copyBtn.setOnLongClickListener {
            if (!recycledList) {
                password.user?.let { it1 -> utils.copyToClipboard(context, it1, false) }

                holder.copyBtn.setImageDrawable(getDrawable(context,
                    R.drawable.check_done_icon
                ))
                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        holder.copyBtn.setImageDrawable(getDrawable(context,
                            R.drawable.item_copy_light
                        ))
                    }
                }
            }
            true
        }

        // Shows a dialog asking the user if he wants to edit or delete the password
        holder.itemView.setOnLongClickListener {
            if (!recycledList) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(context.getString(R.string.options))
                val options = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))

                builder.setItems(options) { _, selection ->
                    when (selection) {
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
            val itemContext = holder.itemView.context as Activity
            startActivity(itemContext, Intent(itemContext, ItemDetailsActivity::class.java).apply { putExtra("id", id) }, null)
            itemContext.overridePendingTransition(R.anim.zoom_in, R.anim.stay)
        }
    }

    /**
     * Moves a password to the recycle bin or deletes it permanently if it's already there
     * @param id Password ID
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
     * Starts the password update screen
     * @param context
     * @param id Password ID
     */
    private fun update(context: Context, id: Int){
        val activity = context as Activity
        startActivity(context, Intent(context, NewPasswordActivity::class.java).apply { putExtra("id", id) }, null)
        activity.overridePendingTransition(R.anim.slide_bottom_2, R.anim.slide_top_2)
    }

    /**
     * Refreshes password list
     * @param newPassList Password list
     */
    fun refreshData(newPassList: List<Pass_Item>) {
        passList = newPassList
        notifyDataSetChanged()
    }

    /**
     * Filters passwords by the input text in the searchView
     * @param text String to search for password nicknames
     */
    fun filterByName(text: String) {
        val filteredList = mutableListOf<Pass_Item>()

        for (item in passList) {
            if (item.nickname.contains(text, true))
                filteredList.add(item)
        }
        passList = filteredList
        notifyDataSetChanged()
    }
}