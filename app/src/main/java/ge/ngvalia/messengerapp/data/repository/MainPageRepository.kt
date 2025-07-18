package ge.ngvalia.messengerapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import ge.ngvalia.messengerapp.ui.homepage.Conversation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainPageRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val CHAT_ROOMS_PATH = "chatRooms"
        private const val USERS_PATH = "users"
    }

    init {
        database.goOnline()
    }

    fun getConversationsFlow(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val chatRoomsRef = database.getReference(CHAT_ROOMS_PATH)
            .orderByChild("lastMessageTimestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<Conversation>()

                snapshot.children.forEach { chatRoomSnapshot ->
                    val chatRoomData = chatRoomSnapshot.value as? Map<String, Any>
                    if (chatRoomData != null) {
                        val participants = chatRoomData["participants"] as? List<String>

                        if (participants?.contains(currentUserId) == true) {
                            val otherUserId = participants.find { it != currentUserId }

                            if (otherUserId != null) {
                                getUserInfo(otherUserId) { userInfo ->
                                    val conversation = Conversation(
                                        chatRoomId = chatRoomSnapshot.key ?: "",
                                        otherUserId = otherUserId,
                                        otherUserName = userInfo?.get("nickname") as? String ?: "Unknown User",
                                        otherUserAvatar = userInfo?.get("profilePicUrl") as? String ?: "",
                                        lastMessage = chatRoomData["lastMessage"] as? String ?: "",
                                        lastMessageTime = formatTimestamp(chatRoomData["lastMessageTimestamp"] as? Long ?: 0L),
                                        lastMessageTimestamp = chatRoomData["lastMessageTimestamp"] as? Long ?: 0L,
                                        lastMessageSenderId = chatRoomData["lastMessageSenderId"] as? String ?: "",
                                        isUnread = (chatRoomData["lastMessageSenderId"] as? String) != currentUserId
                                    )

                                    conversations.add(conversation)
                                    val sortedConversations = conversations.sortedByDescending { it.lastMessageTimestamp }
                                    trySend(sortedConversations)
                                }
                            }
                        }
                    }
                }

                if (conversations.isEmpty()) {
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        chatRoomsRef.addValueEventListener(listener)

        awaitClose {
            chatRoomsRef.removeEventListener(listener)
        }
    }

    private fun getUserInfo(userId: String, callback: (Map<String, Any>?) -> Unit) {
        database.getReference("$USERS_PATH/$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.value as? Map<String, Any>
                    callback(userData)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
            diff < 24 * 60 * 60 * 1000 -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    suspend fun searchUsers(query: String): Result<List<Map<String, Any>>> {
        return try {
            val usersRef = database.getReference(USERS_PATH)

            val snapshot = suspendCancellableCoroutine { continuation ->
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }

            val users = mutableListOf<Map<String, Any>>()
            val currentUserId = auth.currentUser?.uid

            snapshot.children.forEach { userSnapshot ->
                val userData = userSnapshot.value as? Map<String, Any>
                val userId = userSnapshot.key

                if (userData != null && userId != currentUserId) {
                    val nickname = userData["nickname"] as? String ?: ""
                    val nicknameLower = userData["nicknameLower"] as? String ?: nickname.lowercase()

                    if (nicknameLower.contains(query.lowercase())) {
                        val userMap = userData.toMutableMap()
                        userMap["uid"] = userId ?: ""
                        users.add(userMap)
                    }
                }
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}