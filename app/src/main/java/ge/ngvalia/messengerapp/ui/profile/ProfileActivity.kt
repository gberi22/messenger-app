package ge.ngvalia.messengerapp.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ge.ngvalia.messengerapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Load actual user data
        binding.etNickname.setText("Sayed Eftiaz")
        binding.etProfession.setText("Manager")

        binding.btnUpdate.setOnClickListener {
            // TODO: Implement update logic
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
        }

        binding.btnSignOut.setOnClickListener {
            // TODO: Sign-out logic here
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
        }
    }
}
