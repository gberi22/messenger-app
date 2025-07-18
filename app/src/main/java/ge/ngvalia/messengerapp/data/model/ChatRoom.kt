package ge.ngvalia.messengerapp.data.model

data class ChatRoom (
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String = "",
)
