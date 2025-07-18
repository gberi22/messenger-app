package ge.ngvalia.messengerapp.data.model

data class User(
    val uid: String = "",
    val nickname: String = "",
    val nicknameLower: String = "",
    val password: String = "",
    val profession: String = "",
    val profilePicUrl: String = ""
)
