package ge.ngvalia.messengerapp.ui.chat

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ge.ngvalia.messengerapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter

    // Extract user data from intent
    private val otherUserId: String by lazy {
        intent.getStringExtra("OTHER_USER_ID") ?: ""
    }

    private val otherUserName: String by lazy {
        intent.getStringExtra("OTHER_USER_NAME") ?: "Chat"
    }

    private val otherUserAvatar: String? by lazy {
        intent.getStringExtra("OTHER_USER_AVATAR")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Validate that we have the required user ID
        if (otherUserId.isEmpty()) {
            Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar(otherUserName)
        setupRecyclerView()
        setupViewModel()
        setupObservers()
        setupClickListeners()

        viewModel.initializeChat(otherUserId)
    }

    private fun setupToolbar(userName: String) {
        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.title = userName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.chatToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerMessages.apply {
            this.layoutManager = layoutManager
            adapter = messageAdapter
            // Improved scroll handling
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom && messageAdapter.itemCount > 0) {
                    post {
                        scrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            messageAdapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerMessages.post {
                    binding.recyclerMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.chatParticipant.observe(this) { participant ->
            participant?.let {
                supportActionBar?.title = it.nickname
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            val hasText = binding.etMessage.text.toString().trim().isNotEmpty()
            binding.btnSend.isEnabled = !isLoading && hasText
            binding.btnSend.alpha = if (!isLoading && hasText) 1.0f else 0.5f
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.messageSent.observe(this) { messageSent ->
            if (messageSent) {
                binding.etMessage.text?.clear()
                viewModel.clearMessageSent()
                // Hide keyboard after sending
                hideKeyboard()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun updateSendButtonState() {
        val hasText = binding.etMessage.text.toString().trim().isNotEmpty()
        val isNotLoading = viewModel.isLoading.value != true
        val isEnabled = hasText && isNotLoading

        binding.btnSend.isEnabled = isEnabled
        binding.btnSend.alpha = if (isEnabled) 1.0f else 0.5f
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isNotEmpty() && viewModel.isLoading.value != true) {
            viewModel.sendMessage(messageText)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }
}