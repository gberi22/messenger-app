package ge.ngvalia.messengerapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ge.ngvalia.messengerapp.data.model.Message
import ge.ngvalia.messengerapp.data.model.ChatRoom
import ge.ngvalia.messengerapp.data.model.ChatParticipant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val CHAT_ROOMS_COLLECTION = "chatRooms"
        private const val MESSAGES_COLLECTION = "messages"
        private const val USERS_COLLECTION = "users"
    }

    suspend fun createOrGetChatRoom(currentUserId: String, otherUserId: String): Result<ChatRoom> {
        return try {
            val participants = listOf(currentUserId, otherUserId).sorted()
            val chatRoomId = generateChatRoomId(participants)

            val existingRoom = firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .get()
                .await()

            if (existingRoom.exists()) {
                val chatRoom = existingRoom.toObject(ChatRoom::class.java)!!
                Result.success(chatRoom)
            } else {
                val newChatRoom = ChatRoom(
                    id = chatRoomId,
                    participants = participants,
                    lastMessage = "",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    lastMessageSenderId = ""
                )

                firestore.collection(CHAT_ROOMS_COLLECTION)
                    .document(chatRoomId)
                    .set(newChatRoom)
                    .await()

                Result.success(newChatRoom)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatRoomId: String, receiverId: String, text: String): Result<Message> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = receiverId,
                text = text,
                timestamp = System.currentTimeMillis(),
                chatRoomId = chatRoomId
            )

            firestore.collection(MESSAGES_COLLECTION)
                .document(message.id)
                .set(message)
                .await()

            updateChatRoomLastMessage(chatRoomId, text, currentUserId)

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesFlow(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection(MESSAGES_COLLECTION)
            .whereEqualTo("chatRoomId", chatRoomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    fun getChatRoomsFlow(userId: String): Flow<List<ChatRoom>> = callbackFlow {
        val listener = firestore.collection(CHAT_ROOMS_COLLECTION)
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val chatRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoom::class.java)
                } ?: emptyList()

                trySend(chatRooms)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getUserInfo(userId: String): Result<ChatParticipant> {
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (userDoc.exists()) {
                val userData = userDoc.data!!
                val participant = ChatParticipant(
                    userId = userId,
                    nickname = userData["nickname"] as? String ?: "",
                    profilePicUrl = userData["profilePicUrl"] as? String ?: ""
                )
                Result.success(participant)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
        val updates = mapOf(
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to System.currentTimeMillis(),
            "lastMessageSenderId" to senderId
        )

        firestore.collection(CHAT_ROOMS_COLLECTION)
            .document(chatRoomId)
            .update(updates)
            .await()
    }
}
