package ge.ngvalia.messengerapp.ui.homepage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.ngvalia.messengerapp.data.repository.MainPageRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainPageViewModel : ViewModel() {
    private val repository = MainPageRepository()

    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> = _conversations

    private val _filteredConversations = MutableLiveData<List<Conversation>>()
    val filteredConversations: LiveData<List<Conversation>> = _filteredConversations

    private val _searchResults = MutableLiveData<List<Map<String, Any>>>()
    val searchResults: LiveData<List<Map<String, Any>>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isSearchMode = MutableLiveData<Boolean>()
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    private var allConversations: List<Conversation> = emptyList()
    private var currentQuery: String = ""

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getConversationsFlow()
                .catch { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
                .collect { conversationList ->
                    allConversations = conversationList
                    _conversations.value = conversationList

                    // Apply current search if any
                    if (currentQuery.isNotEmpty()) {
                        filterConversations(currentQuery)
                    } else {
                        _filteredConversations.value = conversationList
                    }

                    _isLoading.value = false
                }
        }
    }

    fun search(query: String) {
        currentQuery = query.trim()

        if (currentQuery.isEmpty()) {
            // Clear search mode
            _isSearchMode.value = false
            _filteredConversations.value = allConversations
            _searchResults.value = emptyList()
            return
        }

        if (currentQuery.length < 2) {
            // Too short to search
            return
        }

        _isSearchMode.value = true

        // Filter existing conversations
        filterConversations(currentQuery)

        // Search for new users
        searchUsers(currentQuery)
    }

    private fun filterConversations(query: String) {
        val filtered = allConversations.filter { conversation ->
            conversation.matchesQuery(query)
        }
        _filteredConversations.value = filtered
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            repository.searchUsers(query).fold(
                onSuccess = { users ->
                    // Filter out users we already have conversations with
                    val existingUserIds = allConversations.map { it.otherUserId }.toSet()
                    val newUsers = users.filter { user ->
                        val userId = user["uid"] as? String
                        userId != null && !existingUserIds.contains(userId)
                    }
                    _searchResults.value = newUsers
                },
                onFailure = { exception ->
                    _error.value = "Search failed: ${exception.message}"
                }
            )
        }
    }

    fun clearSearch() {
        currentQuery = ""
        _isSearchMode.value = false
        _filteredConversations.value = allConversations
        _searchResults.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    fun refresh() {
        loadConversations()
    }
}