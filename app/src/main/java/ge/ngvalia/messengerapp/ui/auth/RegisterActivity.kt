package ge.ngvalia.messengerapp.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ge.ngvalia.messengerapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO Back logic later
    }
}
