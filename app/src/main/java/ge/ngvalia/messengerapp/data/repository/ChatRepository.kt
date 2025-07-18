package ge.ngvalia.messengerapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import ge.ngvalia.messengerapp.data.model.Message
import ge.ngvalia.messengerapp.data.model.ChatRoom
import ge.ngvalia.messengerapp.data.model.ChatParticipant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "ChatRepository"
        private const val CHAT_ROOMS_PATH = "chatRooms"
        private const val MESSAGES_PATH = "messages"
        private const val USERS_PATH = "users"
    }

    init {
        enableDatabaseConnection()
    }

    private fun enableDatabaseConnection() {
        try {
            database.goOnline()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable database connection", e)
        }
    }

    suspend fun createOrGetChatRoom(currentUserId: String, otherUserId: String): Result<ChatRoom> {
        return try {
            database.goOnline()

            val participants = listOf(currentUserId, otherUserId).sorted()
            val chatRoomId = generateChatRoomId(participants)
            val chatRoomRef = database.getReference("$CHAT_ROOMS_PATH/$chatRoomId")

            val existingRoom = suspendCancellableCoroutine<DataSnapshot> { continuation ->
                chatRoomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }

            if (existingRoom.exists()) {
                val chatRoom = existingRoom.getValue(ChatRoom::class.java)
                if (chatRoom != null) {
                    Result.success(chatRoom)
                } else {
                    Result.failure(Exception("Failed to load existing chat room"))
                }
            } else {
                val newChatRoom = ChatRoom(
                    id = chatRoomId,
                    participants = participants,
                    lastMessage = "",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    lastMessageSenderId = ""
                )

                suspendCancellableCoroutine<Void?> { continuation ->
                    chatRoomRef.setValue(newChatRoom) { error, _ ->
                        if (error != null) {
                            continuation.resumeWithException(error.toException())
                        } else {
                            continuation.resume(null)
                        }
                    }
                }

                Result.success(newChatRoom)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("disconnected") == true -> "You're offline. Please check your internet connection."
                e.message?.contains("permission") == true -> "Permission denied. Please check your account permissions."
                e.message?.contains("network") == true -> "Network error. Please check your internet connection."
                else -> e.message ?: "Unknown error occurred"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun sendMessage(chatRoomId: String, receiverId: String, text: String): Result<Message> {
        return try {
            database.goOnline()

            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val messageId = database.getReference(MESSAGES_PATH).push().key
                ?: return Result.failure(Exception("Failed to generate message ID"))

            val message = Message(
                id = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                text = text,
                timestamp = System.currentTimeMillis(),
                chatRoomId = chatRoomId
            )

            val messageRef = database.getReference("$MESSAGES_PATH/$chatRoomId/$messageId")

            suspendCancellableCoroutine<Void?> { continuation ->
                messageRef.setValue(message) { error, _ ->
                    if (error != null) {
                        continuation.resumeWithException(error.toException())
                    } else {
                        continuation.resume(null)
                    }
                }
            }

            updateChatRoomLastMessage(chatRoomId, text, currentUserId)
            Result.success(message)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("disconnected") == true -> "You're offline. Message will be sent when connection is restored."
                e.message?.contains("permission") == true -> "Permission denied. Please check your account permissions."
                e.message?.contains("network") == true -> "Network error. Please check your internet connection."
                else -> "Failed to send message: ${e.message}"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    fun getMessagesFlow(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        Log.d(TAG, "Setting up message listener for room: $chatRoomId")

        // Ensure we're online
        database.goOnline()

        val messagesRef = database.getReference("$MESSAGES_PATH/$chatRoomId")
            .orderByChild("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { childSnapshot ->
                    try {
                        childSnapshot.getValue(Message::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing message: ${childSnapshot.key}", e)
                        null
                    }
                }.sortedBy { it.timestamp }

                Log.d(TAG, "Received ${messages.size} messages for room: $chatRoomId")
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error listening to messages", error.toException())
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Closing message listener for room: $chatRoomId")
            messagesRef.removeEventListener(listener)
        }
    }

    suspend fun getUserInfo(userId: String): Result<ChatParticipant> {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId")

            val userSnapshot = suspendCancellableCoroutine<DataSnapshot> { continuation ->
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }

            if (userSnapshot.exists()) {
                val userData = userSnapshot.value as? Map<String, Any>
                if (userData != null) {
                    val participant = ChatParticipant(
                        userId = userId,
                        nickname = userData["nickname"] as? String ?: "",
                        profession = userData["profession"] as? String ?: "",
                        profilePicUrl = userData["profilePicUrl"] as? String ?: ""
                    )

                    Log.d(TAG, "Retrieved user info: ${participant.nickname}")
                    Result.success(participant)
                } else {
                    Result.failure(Exception("Invalid user data format"))
                }
            } else {
                Log.w(TAG, "User document not found: $userId")
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user info for: $userId", e)

            val errorMessage = when {
                e.message?.contains("disconnected") == true -> "You're offline. Please check your internet connection."
                e.message?.contains("permission") == true -> "Permission denied to access user data."
                else -> "Failed to load user information"
            }

            Result.failure(Exception(errorMessage))
        }
    }


    private fun generateChatRoomId(participants: List<String>): String {
        return participants.joinToString("_")
    }

    private suspend fun updateChatRoomLastMessage(
        chatRoomId: String,
        lastMessage: String,
        senderId: String
    ) {
        try {
            val updates = mapOf(
                "lastMessage" to lastMessage,
                "lastMessageTimestamp" to System.currentTimeMillis(),
                "lastMessageSenderId" to senderId
            )

            val chatRoomRef = database.getReference("$CHAT_ROOMS_PATH/$chatRoomId")

            suspendCancellableCoroutine<Void?> { continuation ->
                chatRoomRef.updateChildren(updates) { error, _ ->
                    if (error != null) {
                        Log.e(TAG, "Error updating last message for room: $chatRoomId", error.toException())
                    }
                    continuation.resume(null)
                }
            }

            Log.d(TAG, "Updated last message for room: $chatRoomId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last message for room: $chatRoomId", e)
        }
    }
}