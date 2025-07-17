package ge.ngvalia.messengerapp.ui.profile

import ge.ngvalia.messengerapp.data.model.User
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ge.ngvalia.messengerapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isUpdateSuccessful = MutableLiveData<Boolean>()
    val isUpdateSuccessful: LiveData<Boolean> = _isUpdateSuccessful

    private val randomProfilePictures = listOf(
        "https://twinfinite.net/wp-content/uploads/2022/06/maxresdefault-23-1.jpg?fit=1200%2C675",
        "https://cdn.mos.cms.futurecdn.net/irPnszAULAkCtwFNYVTnxM.jpg",
        "https://www.liveabout.com/thmb/F5lfgFptU9DNTDCT-xNEtot0lQ0=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/EP2-IA-60435_R_8x10-56a83bea3df78cf7729d314a.jpg",
        "https://cdn.nba.com/headshots/nba/latest/1040x760/2544.png",
        "https://s.yimg.com/ny/api/res/1.2/gsm1YzVsDN0QAc0RiTGKbw--/YXBwaWQ9aGlnaGxhbmRlcjt3PTY0MDtoPTQyNztjZj13ZWJw/https://media.zenfs.com/en/athlon_sports_articles_610/ab72a8eee01d11cd5e8c4727809a9bd6",
        "https://ichef.bbci.co.uk/ace/standard/1024/cpsprodpb/7bdc/live/a0ebf160-5d8b-11f0-8868-2da3ea1b26b8.jpg",
        "https://static.wikia.nocookie.net/f1wikia/images/5/57/Hamilton_2025.jpg/revision/latest?cb=20250317005150"
    )

    fun loadUserProfile() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUserProfile(currentUserId).fold(
                onSuccess = { user ->
                    _user.value = user
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateProfile(nickname: String, profession: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val updates = mapOf(
                "nickname" to nickname.trim(),
                "profession" to profession.trim()
            )

            userRepository.updateUserProfile(currentUserId, updates).fold(
                onSuccess = {
                    _isUpdateSuccessful.value = true
                    loadUserProfile()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateProfileImage() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _error.value = "User not logged in"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val randomPicUrl = randomProfilePictures.random()
            val updates = mapOf("profilePicUrl" to randomPicUrl)
            userRepository.updateUserProfile(currentUserId, updates).fold(
                onSuccess = {
                    _isUpdateSuccessful.value = true
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateSuccess() {
        _isUpdateSuccessful.value = false
    }
}
