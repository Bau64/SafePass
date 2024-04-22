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

class New_Password_Generator_Fragment : Fragment() {
    private lateinit var binding: FragmentNewPasswordGeneratorBinding

    var errorSize = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPasswordGeneratorBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    fun initUI(){
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.newPasswordSize.doOnTextChanged {text, _, _, _ ->
            // Se verifica que el texto del tamaño no este vacio y este entre 4 y 64
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
            binding.buttonSaveGenerated.isEnabled = !text.isNullOrEmpty() // Si hay texto en el campo de generar contraseña, se habilita el boton de guardar
        }

        binding.buttonGenerate.setOnClickListener {
            generatePassword()
        }

        binding.buttonSaveGenerated.setOnClickListener {
            saveGeneratedPassword()
        }

        // Sumar +1 al tamaño
        binding.imageButtonAdd.setOnClickListener {
            val sizeText = binding.newPasswordSize.text.toString()
            if (sizeText.isNotEmpty()){
                val newSize = sizeText.toInt() + 1
                binding.newPasswordSize.setText(newSize.toString())
            }
        }

        // Restar -1 al tamaño
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
     * Activa o desactiva el boton de generar en caso de encontrarse un error presente
     */
    fun saveButtonChangeState(){
        if (errorSize || checkBoxes())
            binding.buttonGenerate.isEnabled = false
        else
            binding.buttonGenerate.isEnabled = true

    }

    /**
     * Guardar contraseña generada y enviarsela al fragment anterior en forma de bundle
     */
    fun saveGeneratedPassword(){
        val generatedPassword = binding.generatedPasswordField.text.toString()
        val bundle = Bundle()
        bundle.putString("generatedPassword", generatedPassword)
        parentFragmentManager.setFragmentResult("dataFromGenerator", bundle)
        parentFragmentManager.popBackStack()
    }

    /**
     * Generar contraseña aleatoria
     */
    fun generatePassword(){
        val passwordSize = binding.newPasswordSize.text.toString().toInt() // Tamaño elegido
        var checkBoxOptions = mutableListOf<Char>() // Lista donde estaran los caracteres a usar para crear la contraseña

        if (binding.checkBox.isChecked) // Minusculas
            checkBoxOptions.addAll('a'..'z')

        if (binding.checkBox2.isChecked) // Mayusculas
            checkBoxOptions.addAll('A'..'Z')

        if (binding.checkBox3.isChecked) // Simbolos
            checkBoxOptions.addAll("¡!$%&/(){}[]=¿?*,.;:-_^#~".toList())

        if (binding.checkBox4.isChecked) // Números
            checkBoxOptions.addAll('0'..'9')

        val generatedPassword = buildString {
            repeat(passwordSize) { // Repite la accion de añadir un caracter aleatorio el numero de veces segun el tamaño
                val random = Random.nextInt(0, checkBoxOptions.size) // Elige un caracter aleatorio de la mutableList
                append(checkBoxOptions[random]) // Añade el caracter a la string "generatedPassword"
            }
        }

        binding.generatedPasswordField.setText(generatedPassword)
    }

    /**
     * Verifica si hay almenos una checkbox activa, en el caso contrario devuelve false
     * @return Boolean
     */
    fun checkBoxes():Boolean{
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