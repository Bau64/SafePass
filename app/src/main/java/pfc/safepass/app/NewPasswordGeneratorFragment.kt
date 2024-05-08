package pfc.safepass.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
//import pfc.bautistaczupil.safepass.databinding.FragmentNewPasswordGeneratorBinding
import pfc.safepass.app.databinding.FragmentNewPasswordGeneratorBinding
import kotlin.random.Random

class NewPasswordGeneratorFragment : Fragment() {
    private lateinit var binding: FragmentNewPasswordGeneratorBinding

    private var errorSize = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPasswordGeneratorBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    private fun initUI(){
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.newPasswordSize.doOnTextChanged {text, _, _, _ ->
            // Checks if password text is not empty and is between 4 and 64 characters long
            if (text.isNullOrEmpty()) {
                binding.newPasswordPasswordLayout.error = " "
                binding.newPasswordErrorSize.text = getString(R.string.error_size_empty)
                binding.newPasswordErrorSize.isVisible = true
                errorSize = true
                saveButtonChangeState()
            } else if (text.toString().toInt() < 4 || text.toString().toInt() > 64) {
                binding.newPasswordPasswordLayout.error = " "
                binding.newPasswordErrorSize.text = getString(R.string.error_size)
                binding.newPasswordErrorSize.isVisible = true
                errorSize = true
                saveButtonChangeState()
            } else {
                binding.newPasswordPasswordLayout.error = null
                errorSize = false
                binding.newPasswordErrorSize.isInvisible = true
                saveButtonChangeState()
            }
        }

        binding.generatedPasswordField.doOnTextChanged { text, _, _, _ ->
            binding.buttonSaveGenerated.isEnabled = !text.isNullOrEmpty() // If text is present in the field, the save button becomes enabled
        }

        binding.buttonGenerate.setOnClickListener {
            generatePassword()
        }

        binding.buttonSaveGenerated.setOnClickListener {
            saveGeneratedPassword()
        }

        // Add +1 to size
        binding.imageButtonAdd.setOnClickListener {
            val sizeText = binding.newPasswordSize.text.toString()
            if (sizeText.isNotEmpty()){
                val newSize = sizeText.toInt() + 1
                binding.newPasswordSize.setText(newSize.toString())
            }
        }

        // Subtract -1 to size
        binding.imageButtonSubtract.setOnClickListener {
            val sizeText = binding.newPasswordSize.text.toString()
            if (sizeText.isNotEmpty()){
                val newSize = sizeText.toInt() - 1
                binding.newPasswordSize.setText(newSize.toString())
            }
        }

        binding.checkBox.setOnCheckedChangeListener { _, _ ->
            saveButtonChangeState()
        }

        binding.checkBox2.setOnCheckedChangeListener { _, _ ->
            saveButtonChangeState()
        }

        binding.checkBox3.setOnCheckedChangeListener { _, _ ->
            saveButtonChangeState()
        }

        binding.checkBox4.setOnCheckedChangeListener { _, _ ->
            saveButtonChangeState()
        }
    }

    /**
     * Enables or disables the generate button if there's an error present
     */
    private fun saveButtonChangeState(){
        binding.buttonGenerate.isEnabled = !(errorSize || checkBoxes())
    }

    /**
     * Save generated password and send it to previous fragment in bundle form
     */
    private fun saveGeneratedPassword(){
        val generatedPassword = binding.generatedPasswordField.text.toString()
        val bundle = Bundle()
        bundle.putString("generatedPassword", generatedPassword)
        parentFragmentManager.setFragmentResult("dataFromGenerator", bundle)
        parentFragmentManager.popBackStack()
    }

    /**
     * Generates a random password
     */
    private fun generatePassword(){
        val passwordSize = binding.newPasswordSize.text.toString().toInt() // Tamaño elegido
        val checkBoxOptions = mutableListOf<Char>() // Lista donde estaran los caracteres a usar para crear la contraseña

        if (binding.checkBox.isChecked) // Lowercase
            checkBoxOptions.addAll('a'..'z')

        if (binding.checkBox2.isChecked) // Uppercase
            checkBoxOptions.addAll('A'..'Z')

        if (binding.checkBox3.isChecked) // Symbols
            checkBoxOptions.addAll("¡!$%&/(){}[]=¿?*,.;:-_^#~".toList())

        if (binding.checkBox4.isChecked) // Numbers
            checkBoxOptions.addAll('0'..'9')

        val generatedPassword = buildString {
            repeat(passwordSize) { // Repeats the action of adding a random character the number of times according to size
                val random = Random.nextInt(0, checkBoxOptions.size) // Chooses a random character from the mutableList
                append(checkBoxOptions[random]) // "generatedPassword" Adds the character to the "generatedPassword" String
            }
        }

        binding.generatedPasswordField.setText(generatedPassword)
    }

    /**
     * Verifica si hay almenos una checkbox activa, en el caso contrario devuelve false
     * Checks if there's at least one checked checkbox, otherwise it returns false
     */
    private fun checkBoxes():Boolean{
        var error = false
        if (!binding.checkBox.isChecked && !binding.checkBox2.isChecked && !binding.checkBox3.isChecked && !binding.checkBox4.isChecked){
            error = true
            binding.newPasswordErrorCheckboxes.isVisible = true
        } else {
            binding.newPasswordErrorCheckboxes.isInvisible = true
        }
        return error
    }
}