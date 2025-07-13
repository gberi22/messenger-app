package ge.ngvalia.messengerapp.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ge.ngvalia.messengerapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.title = "Sayed Eftiaz"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.chatToolbar.setNavigationOnClickListener {
            finish()
        }

        // RecyclerView layout manager
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this)

        // TODO: Initialize adapter & set it to recyclerMessages
        // binding.recyclerMessages.adapter = adapter

        // Send button logic
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                // TODO: Add message to chat (send or store locally)
                Toast.makeText(this, "Message sent: $message", Toast.LENGTH_SHORT).show()
                binding.etMessage.text.clear()
            }
        }
    }
}
