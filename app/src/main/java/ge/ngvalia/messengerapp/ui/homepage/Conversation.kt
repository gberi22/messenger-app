package ge.ngvalia.messengerapp.ui.homepage

data class Conversation(
    val userName: String,
    val lastMessage: String,
    val timestamp: String,
    val avatarUrl: String? = null
)

