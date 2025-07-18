package ge.ngvalia.messengerapp.userdiscovery.network

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ge.ngvalia.messengerapp.data.model.User
import kotlinx.coroutines.tasks.await

class FirebaseMigrationHelper {
    private val db = FirebaseDatabase.getInstance().getReference("users")

    suspend fun addNicknameLowerField() {
        val snapshot = db.get().await()

        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null && user.nicknameLower.isEmpty()) {
                val updates = mapOf(
                    "nicknameLower" to user.nickname.lowercase()
                )
                child.ref.updateChildren(updates).await()
            }
        }
    }

    fun addNicknameLowerFieldWithCallback(onComplete: (Boolean, String?) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val updates = mutableListOf<Pair<DatabaseReference, Map<String, Any>>>()

            for (child in snapshot.children) {
                val user = child.getValue(User::class.java)
                if (user != null && user.nicknameLower.isEmpty()) {
                    val updateMap = mapOf(
                        "nicknameLower" to user.nickname.lowercase()
                    )
                    updates.add(Pair(child.ref, updateMap))
                }
            }

            var completed = 0
            var hasError = false

            if (updates.isEmpty()) {
                onComplete(true, "No updates needed")
                return@addOnSuccessListener
            }

            updates.forEach { (ref, updateMap) ->
                ref.updateChildren(updateMap)
                    .addOnSuccessListener {
                        completed++
                        if (completed == updates.size && !hasError) {
                            onComplete(true, "Migration completed successfully")
                        }
                    }
                    .addOnFailureListener { error ->
                        hasError = true
                        onComplete(false, "Migration failed: ${error.message}")
                    }
            }
        }.addOnFailureListener { error ->
            onComplete(false, "Failed to fetch users: ${error.message}")
        }
    }
}
