package ge.ngvalia.messengerapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ge.ngvalia.messengerapp.data.model.Message
import ge.ngvalia.messengerapp.data.model.ChatRoom
import ge.ngvalia.messengerapp.data.model.ChatParticipant
import ge.ngvalia.messengerapp.data.repository.ChatRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _chatParticipant = MutableLiveData<ChatParticipant?>()
    val chatParticipant: LiveData<ChatParticipant?> = _chatParticipant

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent: LiveData<Boolean> = _messageSent

    private var currentChatRoom: ChatRoom? = null
    private var otherUserId: String? = null

    fun initializeChat(otherUserId: String) {
        this.otherUserId = otherUserId
        setupChat()
    }

    private fun setupChat() {
        val currentUserId = auth.currentUser?.uid
        val otherUserId = this.otherUserId

        if (currentUserId == null || otherUserId == null) {
            _error.value = "User not logged in or invalid chat partner"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            chatRepository.createOrGetChatRoom(currentUserId, otherUserId).fold(
                onSuccess = { chatRoom ->
                    currentChatRoom = chatRoom
                    setupMessageListener(chatRoom.id)
                    loadChatParticipant(otherUserId)
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }

    private fun setupMessageListener(chatRoomId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesFlow(chatRoomId)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { messageList ->
                    _messages.value = messageList
                    _isLoading.value = false
                }
        }
    }

    private fun loadChatParticipant(userId: String) {
        viewModelScope.launch {
            chatRepository.getUserInfo(userId).fold(
                onSuccess = { participant ->
                    _chatParticipant.value = participant
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
        }
    }

    fun sendMessage(messageText: String) {
        val chatRoom = currentChatRoom
        val otherUserId = this.otherUserId

        if (chatRoom == null || otherUserId == null) {
            _error.value = "Chat not properly initialized"
            return
        }

        if (messageText.trim().isEmpty()) {
            _error.value = "Message cannot be empty"
            return
        }

        viewModelScope.launch {
            chatRepository.sendMessage(
                chatRoomId = chatRoom.id,
                receiverId = otherUserId,
                text = messageText.trim()
            ).fold(
                onSuccess = {
                    _messageSent.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessageSent() {
        _messageSent.value = false
    }
}
