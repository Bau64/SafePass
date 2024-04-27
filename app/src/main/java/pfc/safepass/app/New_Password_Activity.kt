package pfc.safepass.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pfc.safepass.app.databinding.ActivityNewPasswordBinding

class New_Password_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPasswordBinding
    private lateinit var dataBaseHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataBaseHelper = DataBaseHelper(this)
        initUI()
    }

    private fun initUI(){
        val item_id = intent.getIntExtra("id", -1)
        if (item_id > -1) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, New_Password_Fragment.newInstance(item_id))
                .commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_top_1, R.anim.slide_bottom_1)
    }
}