package ge.ngvalia.messengerapp.data.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Enhanced AuthResult with more specific error types
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()

    // Additional result types you might want
    object NicknameTaken : AuthResult()
    object NetworkError : AuthResult()
    object AuthError : AuthResult()
    object DatabaseError : AuthResult()
}

// Alternative enhanced version with better error handling
class EnhancedAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun registerUser(
        nickname: String,
        password: String,
        profession: String,
        callback: (AuthResult) -> Unit
    ) {
        // Input validation
        if (nickname.isBlank() || password.length < 6) {
            callback(AuthResult.Error("Invalid input: nickname cannot be empty and password must be at least 6 characters"))
            return
        }

        Log.d("AuthRepository", "Starting registration for: $nickname")
        val fakeEmail = "$nickname@messengerapp.com"

        // Step 1: Check nickname availability
        checkNicknameAvailability(nickname) { isAvailable ->
            if (!isAvailable) {
                callback(AuthResult.Error("Nickname '$nickname' is already taken"))
                return@checkNicknameAvailability
            }

            // Step 2: Create auth user
            createAuthUser(fakeEmail, password) { authSuccess, userId, error ->
                if (!authSuccess || userId == null) {
                    callback(AuthResult.Error(error ?: "Authentication failed"))
                    return@createAuthUser
                }

                // Step 3: Create user profile
                createUserProfile(userId, nickname, profession) { profileSuccess, profileError ->
                    if (profileSuccess) {
                        Log.d("AuthRepository", "Registration completed successfully")
                        callback(AuthResult.Success)
                    } else {
                        // Cleanup: delete auth user if profile creation fails
                        auth.currentUser?.delete()
                        callback(AuthResult.Error(profileError ?: "Failed to create user profile"))
                    }
                }
            }
        }
    }

    private fun checkNicknameAvailability(nickname: String, callback: (Boolean) -> Unit) {
        db.child("nicknames").child(nickname).get()
            .addOnSuccessListener { snapshot ->
                callback(!snapshot.exists()) // Available if doesn't exist
            }
            .addOnFailureListener { e ->
                Log.e("AuthRepository", "Error checking nickname availability", e)
                callback(false) // Assume not available on error
            }
    }

    private fun createAuthUser(
        email: String,
        password: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    callback(true, userId, null)
                } else {
                    callback(false, null, "User ID not found after registration")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthRepository", "Auth user creation failed", e)
                callback(false, null, e.message)
            }
    }

    private fun createUserProfile(
        userId: String,
        nickname: String,
        profession: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val userMap = mapOf(
            "uid" to userId,
            "nickname" to nickname,
            "nicknameLower" to nickname.lowercase(),
            "profession" to profession,
            "photoUrl" to "",
            "createdAt" to System.currentTimeMillis()
        )

        val updates = hashMapOf<String, Any>(
            "users/$userId" to userMap,
            "nicknames/$nickname" to userId
        )

        db.updateChildren(updates)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("AuthRepository", "User profile creation failed", e)
                callback(false, e.message)
            }
    }
}