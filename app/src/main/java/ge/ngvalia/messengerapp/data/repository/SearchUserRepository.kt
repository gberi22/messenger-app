package ge.ngvalia.messengerapp.data.repository

import ge.ngvalia.messengerapp.data.model.User
import ge.ngvalia.messengerapp.userdiscovery.network.UserApi

class SearchUserRepository(private val userApi: UserApi) {

    suspend fun getUsers(
        lastKey: String? = null,
        limit: Int = 20
    ): List<User> {
        return userApi.getUsers(startAfterKey = lastKey, limit = limit)
    }

    suspend fun searchUsers(searchQuery: String): List<User> {
        return when {
            searchQuery.length >= 3 -> userApi.searchUsersByNickname(searchQuery)
            else -> emptyList()
        }
    }
}
