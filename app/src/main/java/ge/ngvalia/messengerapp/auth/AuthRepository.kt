package ge.ngvalia.messengerapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ge.ngvalia.messengerapp.data.model.User
import ge.ngvalia.messengerapp.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val userRepository = UserRepository()

    fun registerUser(
        nickname: String,
        password: String,
        profession: String,
        callback: (AuthResult) -> Unit
    ) {
        val fakeEmail = "$nickname@messengerapp.com"
        db.child("nicknames").child(nickname).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                callback(AuthResult.Error("Nickname already taken"))
            } else {
                auth.createUserWithEmailAndPassword(fakeEmail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                callback(AuthResult.Error("Registration failed: user not found"))
                                return@addOnCompleteListener
                            }

                            // Create user data with nicknameLower for search
                            val userMap = mapOf(
                                "uid" to userId,
                                "nickname" to nickname,
                                "nicknameLower" to nickname.lowercase(), // Added for search
                                "profession" to profession,
                                "photoUrl" to "" // Added for completeness
                            )
                            // Create user profile
                            val user = User(
                                uid = userId,
                                nickname = nickname,
                                profession = profession,
                                profilePicUrl = ""
                            )

                            val updates = hashMapOf<String, Any>(
                                "users/$userId" to userMap,
                                "nicknames/$nickname" to userId
                            )

                            Log.d("AuthRepository", "Updating database with: $updates")

                            db.updateChildren(updates)
                                .addOnSuccessListener {
                                    Log.d("AuthRepository", "Database update successful")
                                    callback(AuthResult.Success)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AuthRepository", "DB update failed: ${e.message}")
                                    // Delete the auth user if DB update fails
                                    auth.currentUser?.delete()
                                    callback(AuthResult.Error(e.message ?: "Failed to create user profile"))
                                }

                            CoroutineScope(Dispatchers.IO).launch {
                                userRepository.createUserProfile(user).fold(
                                    onSuccess = {
                                        callback(AuthResult.Success)
                                    },
                                    onFailure = { exception ->
                                        callback(AuthResult.Error(exception.message ?: "Profile creation failed"))
                                    }
                                )
                            }
                        } else {
                            Log.e("AuthRepository", "Authentication failed: ${task.exception?.message}")
                            callback(
                                AuthResult.Error(
                                    task.exception?.message ?: "Registration failed"
                                )
                            )
                        }
                    }
            }
        }.addOnFailureListener { e ->
            callback(AuthResult.Error(e.message ?: "DB error"))
        }
    }

    fun loginUser(
        nickname: String,
        password: String,
        callback: (AuthResult) -> Unit
    ) {
        val fakeEmail = "$nickname@messengerapp.com"
        auth.signInWithEmailAndPassword(fakeEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(AuthResult.Success)
                } else {
                    callback(
                        AuthResult.Error(
                            task.exception?.localizedMessage ?: "Login failed"
                        )
                    )
                }
            }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}