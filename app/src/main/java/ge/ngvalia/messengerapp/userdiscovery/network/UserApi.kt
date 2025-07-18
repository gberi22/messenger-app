package ge.ngvalia.messengerapp.userdiscovery.network

import com.google.firebase.database.*
import ge.ngvalia.messengerapp.data.model.User
import kotlinx.coroutines.tasks.await

class UserApi(
    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
) {

    suspend fun getUsers(
        startAfterKey: String? = null,
        limit: Int = 20
    ): List<User> {
        val query: Query = if (startAfterKey != null) {
            db.orderByKey().startAfter(startAfterKey).limitToFirst(limit)
        } else {
            db.orderByKey().limitToFirst(limit)
        }

        val snapshot = query.get().await()
        return processUsersFromSnapshot(snapshot)
    }

    suspend fun searchUsersByNickname(searchQuery: String, limit: Int = 50): List<User> {
        val searchQueryLower = searchQuery.lowercase()

        // Option 1: If you have nicknameLower field in Firebase
        return searchByNicknameLowerField(searchQueryLower, limit)

        // Option 2: If you only have nickname field (fallback)
        // return searchByNicknameFieldWithClientFilter(searchQuery, limit)
    }

    private suspend fun searchByNicknameLowerField(searchQueryLower: String, limit: Int): List<User> {
        val query = db.orderByChild("nicknameLower")
            .startAt(searchQueryLower)
            .endAt(searchQueryLower + "\uf8ff")
            .limitToFirst(limit)

        val snapshot = query.get().await()
        return processUsersFromSnapshot(snapshot)
    }

    private suspend fun searchByNicknameFieldWithClientFilter(searchQuery: String, limit: Int): List<User> {
        val searchQueryLower = searchQuery.lowercase()

        val query = db.orderByChild("nickname")
            .startAt(searchQuery)
            .endAt(searchQuery + "\uf8ff")
            .limitToFirst(limit * 2) // Get more to account for case filtering

        val snapshot = query.get().await()
        val users = processUsersFromSnapshot(snapshot)

        return users.filter { user ->
            user.nickname.lowercase().contains(searchQueryLower)
        }.take(limit)
    }

    // Alternative: Get all users and filter client-side (only for small datasets)
    suspend fun searchUsersClientSide(searchQuery: String): List<User> {
        val searchQueryLower = searchQuery.lowercase()

        // Warning: This loads ALL users - only use for small datasets
        val snapshot = db.get().await()
        val users = processUsersFromSnapshot(snapshot)

        return users.filter { user ->
            user.nickname.lowercase().contains(searchQueryLower)
        }
    }

    private fun processUsersFromSnapshot(snapshot: DataSnapshot): List<User> {
        val users = mutableListOf<User>()

        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null) {
                val userWithId = user.copy(
                    uid = child.key ?: "",
                    // If nickname is empty, try to use the key as nickname
                    nickname = if (user.nickname.isEmpty()) child.key ?: "" else user.nickname,
                    // Ensure nicknameLower is set
                    nicknameLower = if (user.nicknameLower.isEmpty()) {
                        (if (user.nickname.isEmpty()) child.key ?: "" else user.nickname).lowercase()
                    } else user.nicknameLower
                )
                users.add(userWithId)
            }
        }
        return users
    }
}