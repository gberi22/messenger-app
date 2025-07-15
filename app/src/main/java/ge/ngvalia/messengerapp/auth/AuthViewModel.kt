package ge.ngvalia.messengerapp.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun register(nickname: String, password: String, profession: String) {
        repository.registerUser(nickname, password, profession) { result ->
            _authResult.postValue(result)
        }
    }

    fun login(nickname: String, password: String) {
        repository.loginUser(nickname, password) { result ->
            _authResult.postValue(result)
        }
    }

    fun logout() {
        repository.logoutUser()
    }

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()
}
