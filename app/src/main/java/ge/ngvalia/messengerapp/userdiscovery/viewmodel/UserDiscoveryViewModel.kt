package ge.ngvalia.messengerapp.userdiscovery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.ngvalia.messengerapp.data.model.User
import ge.ngvalia.messengerapp.data.repository.SearchUserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserDiscoveryViewModel(
    private val repository: SearchUserRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private var currentUsers = mutableListOf<User>()
    private var lastKey: String? = null
    private var isSearchMode = false
    private var searchJob: Job? = null
    private var currentSearchQuery = ""

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val newUsers = repository.getUsers(lastKey = lastKey)

                if (lastKey == null) {
                    currentUsers.clear()
                }

                currentUsers.addAll(newUsers)

                if (!isSearchMode) {
                    _users.value = currentUsers.toList()
                }

                lastKey = newUsers.lastOrNull()?.uid

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        currentSearchQuery = query.trim()

        searchJob?.cancel()

        if (currentSearchQuery.isEmpty()) {
            clearSearch()
            return
        }

        searchJob = viewModelScope.launch {
            try {
                delay(300)

                _isSearching.value = true
                _error.value = null
                isSearchMode = true

                val searchResults = repository.searchUsers(currentSearchQuery)
                _users.value = searchResults

            } catch (e: Exception) {
                _error.value = e.message
                _users.value = currentUsers.toList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        currentSearchQuery = ""
        isSearchMode = false
        _isSearching.value = false
        _users.value = currentUsers.toList()
    }

    fun onScrolledToEnd() {
        if (!isSearchMode && _isLoading.value != true) {
            loadUsers()
        }
    }

    fun refresh() {
        lastKey = null
        currentUsers.clear()
        if (isSearchMode && currentSearchQuery.isNotEmpty()) {
            searchUsers(currentSearchQuery)
        } else {
            isSearchMode = false
            loadUsers()
        }
    }
}
