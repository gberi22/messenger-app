package ge.ngvalia.messengerapp.data.model

data class Message (
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val chatRoomId: String = ""
)
