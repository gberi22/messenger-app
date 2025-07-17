package ge.ngvalia.messengerapp.userdiscovery.data.repository

import ge.ngvalia.messengerapp.userdiscovery.data.model.User
import ge.ngvalia.messengerapp.userdiscovery.network.UserApi

class UserRepository(private val userApi: UserApi) {

    suspend fun getUsers(
        lastKey: String? = null,
        limit: Int = 20
    ): List<User> {
        return userApi.getUsers(startAfterKey = lastKey, limit = limit)
    }

    suspend fun searchUsers(searchQuery: String): List<User> {
        return when {
            searchQuery.length >= 2 -> userApi.searchUsersByNickname(searchQuery)
            else -> emptyList()
        }
    }
}