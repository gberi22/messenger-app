package ge.ngvalia.messengerapp.ui.chat

import android.content.Context
import android.content.Intent

object ChatUtils {

    fun startChatActivity(
        context: Context,
        otherUserId: String,
        otherUserName: String,
        otherUserAvatar: String? = null,
        otherUserProfession: String? = null
    ) {
        try {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("OTHER_USER_ID", otherUserId)
                putExtra("OTHER_USER_NAME", otherUserName)
                putExtra("OTHER_USER_AVATAR", otherUserAvatar)
                putExtra("OTHER_USER_PROFESSION", otherUserProfession)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            throw e
        }
    }
}