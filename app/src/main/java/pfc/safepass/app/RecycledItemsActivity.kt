package pfc.safepass.app

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import pfc.safepass.app.databinding.ActivityRecycledItemsBinding

class RecycledItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecycledItemsBinding
    private lateinit var dataBaseHelper: DataBaseHelper
    private lateinit var passwordAdapter: PasswordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycledItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar3)
        initUI()
    }

    fun initUI() {
        dataBaseHelper = DataBaseHelper(this)
        passwordAdapter = PasswordAdapter(dataBaseHelper.getAllPassword(true), this, true)
        binding.recyclerview2.layoutManager = LinearLayoutManager(this)
        binding.recyclerview2.adapter = passwordAdapter
        recycler_helper()
    }

    fun recycler_helper() {
        if (passwordAdapter.itemCount == 0)
            binding.recycledHint.isVisible = true
        else
            binding.recycledHint.isGone = true
    }
}