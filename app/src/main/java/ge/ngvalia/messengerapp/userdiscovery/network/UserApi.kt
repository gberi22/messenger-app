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

        return searchByNicknameLowerField(searchQueryLower, limit)
    }

    private suspend fun searchByNicknameLowerField(searchQueryLower: String, limit: Int): List<User> {
        val query = db.orderByChild("nicknameLower")
            .startAt(searchQueryLower)
            .endAt(searchQueryLower + "\uf8ff")
            .limitToFirst(limit)

        val snapshot = query.get().await()
        return processUsersFromSnapshot(snapshot)
    }

    private fun processUsersFromSnapshot(snapshot: DataSnapshot): List<User> {
        val users = mutableListOf<User>()

        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null) {
                val userWithId = user.copy(
                    uid = child.key ?: "",
                    nickname = if (user.nickname.isEmpty()) child.key ?: "" else user.nickname,
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