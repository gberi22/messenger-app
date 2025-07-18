package ge.ngvalia.messengerapp.ui.homepage

data class Conversation(
    val chatRoomId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastMessageSenderId: String = "",
    val isUnread: Boolean = false
) {
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return otherUserName.lowercase().contains(lowerQuery) ||
                lastMessage.lowercase().contains(lowerQuery)
    }
}