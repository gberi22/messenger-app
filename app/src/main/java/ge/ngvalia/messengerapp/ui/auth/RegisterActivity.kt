package ge.ngvalia.messengerapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ge.ngvalia.messengerapp.databinding.ActivityRegisterBinding
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import ge.ngvalia.messengerapp.R
import ge.ngvalia.messengerapp.auth.AuthResult
import ge.ngvalia.messengerapp.auth.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO Back logic later
        val passwordEdit = binding.etPassword
        val nicknameEdit = binding.etNickname
        val professionEdit = binding.etProfession
        val registerBtn = binding.btnSignUp

        registerBtn.setOnClickListener {
            Log.d("RegisterActivity", "Register button clicked")
            if (nicknameEdit.text.isEmpty() || passwordEdit.text.isEmpty() || professionEdit.text.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nickname = nicknameEdit.text.toString()
            val profession = professionEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (nickname.length < 3) {
                Toast.makeText(this, "Nickname must be at least 3 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            viewModel.register(nickname, password, profession)
        }

        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainPageActivity::class.java))
                    finishAffinity()
                }

                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }
}
