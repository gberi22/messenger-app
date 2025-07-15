package ge.ngvalia.messengerapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun registerUser(
        nickname: String,
        password: String,
        profession: String,
        callback: (AuthResult) -> Unit
    ) {
        Log.d("AuthRepository", "Attempting to register user: $nickname")
        val fakeEmail = "$nickname@messengerapp.com"
        db.child("nicknames").child(nickname).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d("AuthRepository", "if block reached, proceeding with registration")
                callback(AuthResult.Error("Nickname already taken"))
            } else {
                Log.d("AuthRepository", "Else block reached, proceeding with registration")
                auth.createUserWithEmailAndPassword(fakeEmail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("AuthRepository", "User registered successfully")
                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                Log.e("AuthRepository", "auth.currentUser is null after registration!")
                                callback(AuthResult.Error("Registration failed: user not found"))
                                return@addOnCompleteListener
                            }
                            val userMap = mapOf(
                                "uid" to userId,
                                "nickname" to nickname,
                                "profession" to profession
                            )
                            val updates = hashMapOf<String, Any>(
                                "users/$userId" to userMap,
                                "nicknames/$nickname" to userId
                            )
                            db.updateChildren(updates)
                                .addOnSuccessListener { callback(AuthResult.Success) }
                                .addOnFailureListener { e ->
                                    Log.e("AuthRepository", "DB update failed: ${e.message}")
                                    callback(AuthResult.Error(e.message ?: "DB error"))
                                }
                        } else {
                            Log.e("AuthRepository", "Registration failed: ${task.exception?.localizedMessage}")
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

    fun logoutUser() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}