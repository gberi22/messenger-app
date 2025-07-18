package ge.ngvalia.messengerapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import ge.ngvalia.messengerapp.R
import ge.ngvalia.messengerapp.data.model.AuthResult
import ge.ngvalia.messengerapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import ge.ngvalia.messengerapp.ui.homepage.MainPageActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainPageActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val nicknameEdit = findViewById<EditText>(R.id.etNickname)
        val passwordEdit = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        loginBtn.setOnClickListener {
            val nickname = nicknameEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (nickname.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(nickname, password)
        }

        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainPageActivity::class.java))
                    finish()
                }

                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

}