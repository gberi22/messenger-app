package ge.ngvalia.messengerapp.data.repository

import ge.ngvalia.messengerapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            if (!isNicknameUnique(user.nickname)) {
                return Result.failure(Exception("Nickname already exists"))
            }

            val userMap = mapOf(
                "uid" to user.uid,
                "nickname" to user.nickname,
                "profession" to user.profession,
                "profilePicUrl" to user.profilePicUrl,
            )

            val updates = hashMapOf(
                "users/${user.uid}" to userMap,
                "nicknames/${user.nickname}" to user.uid
            )
            database.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val currentUser = getUserProfile(userId).getOrNull()
                ?: return Result.failure(Exception("User not found"))

            if (updates.containsKey("nickname")) {
                val newNickname = updates["nickname"] as String
                val oldNickname = currentUser.nickname
                if (newNickname != oldNickname) {
                    if (!isNicknameUnique(newNickname, userId)) {
                        return Result.failure(Exception("Nickname already exists"))
                    }

                    val updateEmailResult = updateAuthEmail(newNickname)
                    if (updateEmailResult.isFailure) {
                        return Result.failure(updateEmailResult.exceptionOrNull()
                            ?: Exception("Failed to update authentication email"))
                    }

                    database.child("nicknames").child(oldNickname).removeValue().await()
                    database.child("nicknames").child(newNickname).setValue(userId).await()
                }
            }
            val updatedData = updates.toMutableMap()

            database.child("users").child(userId).updateChildren(updatedData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateAuthEmail(newNickname: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("No authenticated user"))

            val newEmail = "$newNickname@messengerapp.com"

            currentUser.updateEmail(newEmail).await()

            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = newNickname
            }
            currentUser.updateProfile(profileUpdates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            suspendCancellableCoroutine { continuation ->
                database.child("users").child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                val userMap = snapshot.value as? Map<*, *>
                                if (userMap != null) {
                                    val user = User(
                                        uid = userMap["uid"] as? String ?: "",
                                        nickname = userMap["nickname"] as? String ?: "",
                                        profession = userMap["profession"] as? String ?: "",
                                        profilePicUrl = userMap["profilePicUrl"] as? String ?: ""
                                    )
                                    continuation.resume(Result.success(user))
                                } else {
                                    continuation.resume(Result.failure(Exception("User not found")))
                                }
                            } catch (e: Exception) {
                                continuation.resume(Result.failure(e))
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(error.toException()))
                        }
                    })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun isNicknameUnique(nickname: String, excludeUserId: String = ""): Boolean {
        return try {
            suspendCancellableCoroutine { continuation ->
                database.child("nicknames").child(nickname)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val existingUserId = snapshot.value as? String
                            val isUnique = existingUserId == null || existingUserId == excludeUserId
                            continuation.resume(isUnique)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(false)
                        }
                    })
            }
        } catch (e: Exception) {
            false
        }
    }
}
