package pfc.safepass.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import pfc.safepass.app.recycler.Pass_Item

class NewPasswordFragment : Fragment() {
    private lateinit var binding: FragmentNewPasswordBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private val utils = Utils()
    private var isCustomImageSet = false
    private lateinit var customImageUri: Uri // Chosen image saved temporally in URI format in case the fragment changes
    private var updateId: Int = -1 // Password ID to update
    private var updateItem: Pass_Item? = null // Password object to update
    private var updateMode = false // false = new password ; true = update password
    private lateinit var defaultImgBytearray: ByteArray // Default byteArray used when the user doesn't choose an icon
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            binding.newPasswordImageView.setImageURI(uriContent) // The image gets applied to the imageview
            isCustomImageSet = true
            customImageUri = uriContent!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            updateId = it.getInt("id", -1)
        }

        // if password ID exists, the password object is obtained from the database
        if (updateId > -1) {
            dataBaseHelper = DataBaseHelper(requireContext())
            updateItem = dataBaseHelper.getPasswordbyID(updateId)
            updateMode = true
        }

        defaultImgBytearray = utils.imgtoByteArray(ContextCompat.getDrawable(requireContext(), R.drawable.icon_default_password)!!.toBitmap())
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Int) =
            NewPasswordFragment().apply {
                arguments = Bundle().apply {
                    putInt("id", id)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPasswordBinding.inflate(layoutInflater)
        dataBaseHelper = DataBaseHelper(requireContext())
        initUI()
        return binding.root
    }

    private fun initUI(){
        if (!isCustomImageSet)
            binding.newPasswordImageView.setImageResource(R.drawable.icon_default_password)
        else
            binding.newPasswordImageView.setImageURI(customImageUri)

        binding.newPasswordImageButton.setOnClickListener {
            startCrop()
        }

        binding.btnGenerate.setOnClickListener {
            utils.hideKeyboard(requireActivity())
            goToPasswordGenerator()
        }

        binding.newPasswordSaveBtn.setOnClickListener {
            savePassword()
        }

        // If a password is being updated, the fields will be filled with the password's data
        if (updateMode) {
            binding.newPasswordSaveBtn.isEnabled = true
            binding.toolbar.title = getString(R.string.toolbar_edit_password)

            binding.newPasswordNickname.setText(updateItem!!.nickname)
            binding.newPasswordPassword.setText(updateItem!!.password)
            binding.newPasswordUsername.setText(updateItem!!.user)
            binding.newPasswordNotes.setText(updateItem!!.notes)

            if (updateItem!!.icon == null)
                binding.newPasswordImageView.setImageResource(R.drawable.icon_default_password)
            else
                binding.newPasswordImageView.setImageBitmap(byteArrayToBitmap(updateItem!!.id))
        }

        // Receive generated password in case the user saved it from the generator
        parentFragmentManager.setFragmentResultListener("dataFromGenerator",this) { _, bundle ->
            val generatedPasswordResult = bundle.getString("generatedPassword")
            binding.newPasswordPassword.setText(generatedPasswordResult)
        }

        // Verifies if the required fields are not empty before generating the password
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
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right)
            .replace(R.id.fragmentContainerView, NewPasswordGeneratorFragment())
            .addToBackStack(null).commit()
    }

    /**
     * Starts image crop activity
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
     * Saves the password created by the user
     */
    private fun savePassword(){
        val nickname = binding.newPasswordNickname.text.toString()
        val username = binding.newPasswordUsername.text.toString()
        val password = binding.newPasswordPassword.text.toString()
        val notes = binding.newPasswordNotes.text.toString()
        var icon: ByteArray? = utils.imgtoByteArray(binding.newPasswordImageView.drawable.toBitmap())

        // Checks if the image is different to the default image using the ByteArray from both images
        if (icon.contentEquals(defaultImgBytearray)) {
            icon = null
        }

        if (updateMode) {
            val passwordItem = Pass_Item(updateItem!!.id, nickname, username, password, notes, icon, utils.getCurrentDate())
            dataBaseHelper.updatePassword(passwordItem)
            Toast.makeText(context, getString(R.string.toast_edited_password), Toast.LENGTH_SHORT).show()
        } else {
            val passwordItem = Pass_Item(0, nickname, username, password, notes, icon, utils.getCurrentDate())
            dataBaseHelper.insertPassword(passwordItem)
            Toast.makeText(context, getString(R.string.toast_created_password), Toast.LENGTH_SHORT).show()
        }

        requireActivity().finish()
    }

    private fun byteArrayToBitmap(id: Int): Bitmap {
        val item = dataBaseHelper.getPasswordbyID(id)
        return BitmapFactory.decodeByteArray(item!!.icon, 0, item.icon!!.size)
    }
}