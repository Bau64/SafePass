package pfc.safepass.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import pfc.safepass.app.databinding.FragmentNewPasswordBinding
//import pfc.bautistaczupil.safepass.databinding.FragmentNewPasswordBinding
import java.io.ByteArrayOutputStream

class New_Password_Fragment : Fragment() {
    private lateinit var binding: FragmentNewPasswordBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private var update_id: Int = -1 // ID de la entrada a actualizar
    private var update_item: Pass_Item? = null // Entrada a actualizar
    private var update_mode = false // false = nueva contraseña ; true = actualizar contraseña
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            binding.newPasswordImageView.setImageURI(uriContent) // Se aplica la imagen al imageview
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            update_id = it.getInt("id", -1)
        }

        // Si el id de la entrada existe, se obtiene la entrada desde la base de datos
        if (update_id > -1) {
            dataBaseHelper = DataBaseHelper(requireContext())
            update_item = dataBaseHelper.getPasswordbyID(update_id)
            update_mode = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Int) =
            New_Password_Fragment().apply {
                arguments = Bundle().apply {
                    putInt("id", id)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPasswordBinding.inflate(layoutInflater)
        dataBaseHelper = DataBaseHelper(requireContext())
        initUI()
        return binding.root
    }

    private fun initUI(){
        binding.newPasswordImageButton.setOnClickListener {
            startCrop()
        }

        binding.btnGenerate.setOnClickListener {
            goToPasswordGenerator()
        }

        binding.newPasswordSaveBtn.setOnClickListener {
            savePassword()
        }

        // Si se esta actualizando una entrada, los campos se rellenaran con los datos de la entrada
        if (update_mode) {
            binding.toolbar.title = getString(R.string.toolbar_edit_password)
            binding.newPasswordImageView.setImageBitmap(byteArrayToBitmap(update_item!!.id))
            binding.newPasswordNickname.setText(update_item!!.nickname)
            binding.newPasswordPassword.setText(update_item!!.password)
            binding.newPasswordUsername.setText(update_item!!.user)
            binding.newPasswordLink.setText(update_item!!.link)
        }

        // Recibir contraseña generada en caso de haberse guardado en el generador
        parentFragmentManager.setFragmentResultListener("dataFromGenerator",this) { _, bundle ->
            val generatedPasswordResult = bundle.getString("generatedPassword")
            binding.newPasswordPassword.setText(generatedPasswordResult)
        }

        // Verifica si los campos obligatorios no estan vacios antes de generar la contraseña
        binding.newPasswordNickname.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()){
                binding.newPasswordSaveBtn.isEnabled = false
                binding.newPasswordNicknameLayout.error = getString(R.string.error_empty_nickname)
            } else {
                binding.newPasswordNicknameLayout.error = null
                if (text.isNotEmpty() && binding.newPasswordPassword.text.toString().isNotEmpty())
                    binding.newPasswordSaveBtn.isEnabled = true
            }
        }

        binding.newPasswordPassword.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()){
                binding.newPasswordSaveBtn.isEnabled = false
                binding.newPasswordPasswordLayout.error = getString(R.string.error_empty_password)
            } else {
                binding.newPasswordPasswordLayout.error = null
                if (text.isNotEmpty() && binding.newPasswordNickname.text.toString().isNotEmpty())
                    binding.newPasswordSaveBtn.isEnabled = true
            }
        }
    }

    private fun goToPasswordGenerator(){
        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, New_Password_Generator_Fragment())
            .addToBackStack(null).commit()
    }

    /**
     * Inicia la actividad de recorte de imagenes
     */
    private fun startCrop() {
        cropImage.launch(
            CropImageContractOptions(uri = null, cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON, // Activar rejilla de zona a recortar
                imageSourceIncludeGallery = true, // Elegir imagen de la galeria
                imageSourceIncludeCamera = false, // No elegir imagen de la camara
                cropShape = CropImageView.CropShape.OVAL, // Forma de circulo
                outputCompressFormat = Bitmap.CompressFormat.PNG, // Formato PNG para la imagen
                activityBackgroundColor = Color.BLACK, // Color de fondo
                toolbarColor = ContextCompat.getColor(requireContext(), R.color.app_blue), // Color de la barra
                fixAspectRatio = true, // Evita que se pueda deformar el area de recorte
                activityTitle = getString(R.string.toolbar_cropTitle)
            ))
        )
    }

    /**
     * Guarda la contraseña introducida por el usuario
     */
    private fun savePassword(){
        val nickname = binding.newPasswordNickname.text.toString()
        val username = binding.newPasswordUsername.text.toString()
        val icon = imgViewtoByteArray(binding.newPasswordImageView)
        val password = binding.newPasswordPassword.text.toString()
        val link = binding.newPasswordLink.text.toString()

        if (update_mode) {
            val passwordItem = Pass_Item(update_item!!.id, nickname, username, password, link, icon)
            dataBaseHelper.updatePassword(passwordItem)
            val activity = requireActivity() as MainMenuActivity
            activity.refreshData()
            Toast.makeText(context, getString(R.string.toast_edited_password), Toast.LENGTH_SHORT).show()
        } else {
            val passwordItem = Pass_Item(0, nickname, username, password, link, icon)
            dataBaseHelper.insertPassword(passwordItem)
            Toast.makeText(context, getString(R.string.toast_deleted_password), Toast.LENGTH_SHORT).show()
        }

        requireActivity().finish()
    }

    fun imgViewtoByteArray(imgview: ImageView): ByteArray {
        val bmp: Bitmap = imgview.drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(id: Int): Bitmap {
        val item = dataBaseHelper.getPasswordbyID(id)
        return BitmapFactory.decodeByteArray(item!!.icon, 0, item.icon.size)
    }
}