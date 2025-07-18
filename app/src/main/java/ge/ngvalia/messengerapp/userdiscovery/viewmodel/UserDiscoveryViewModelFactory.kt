package ge.ngvalia.messengerapp.userdiscovery.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ge.ngvalia.messengerapp.data.repository.SearchUserRepository

class UserDiscoveryViewModelFactory(
    private val repository: SearchUserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDiscoveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDiscoveryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}