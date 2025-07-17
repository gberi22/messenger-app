package ge.ngvalia.messengerapp.userdiscovery.data.model

data class User(
    val id: String = "",
    val nickname: String = "",
    val nicknameLower: String = "",
    val profession: String? = null,
    val photoUrl: String? = null,
)