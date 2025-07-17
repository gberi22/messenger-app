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

                            // Create user profile
                            val user = User(
                                uid = userId,
                                nickname = nickname,
                                profession = profession,
                                profilePicUrl = ""
                            )

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
                            callback(
                                AuthResult.Error(
                                    task.exception?.localizedMessage ?: "Auth error"
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
}
