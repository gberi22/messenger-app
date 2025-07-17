package ge.ngvalia.messengerapp.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import ge.ngvalia.messengerapp.R
import ge.ngvalia.messengerapp.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import ge.ngvalia.messengerapp.MainPageActivity
import ge.ngvalia.messengerapp.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var bottomNavigationView: BottomNavigationView

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfileImage)

                viewModel.updateProfileImage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupBottomNavigation()
        setupClickListeners()
        observeViewModel()

        viewModel.loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_profile
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainPageActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.nav_profile
    }

    private fun setupClickListeners() {
        binding.ivProfileImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun observeViewModel() {
        viewModel.user.observe(this) { user ->
            user?.let {
                binding.etNickname.setText(it.nickname)
                binding.etProfession.setText(it.profession)

                if (it.profilePicUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(it.profilePicUrl)
                        .placeholder(R.drawable.profile_picture)
                        .error(R.drawable.profile_picture)
                        .circleCrop()
                        .into(binding.ivProfileImage)
                } else {
                    binding.ivProfileImage.setImageResource(R.drawable.profile_picture)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnUpdate.isEnabled = !isLoading
            binding.btnSignOut.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.isUpdateSuccessful.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                viewModel.clearUpdateSuccess()
            }
        }
    }

    private fun updateProfile() {
        val nickname = binding.etNickname.text.toString()
        val profession = binding.etProfession.text.toString()

        when {
            nickname.isBlank() -> {
                Toast.makeText(this, "Please enter a nickname", Toast.LENGTH_SHORT).show()
                return
            }
            profession.isBlank() -> {
                Toast.makeText(this, "Please enter a profession", Toast.LENGTH_SHORT).show()
                return
            }
            nickname.length < 3 -> {
                Toast.makeText(this, "Nickname must be at least 3 characters", Toast.LENGTH_SHORT).show()
                return
            }
        }

        viewModel.updateProfile(nickname, profession)
    }
}
