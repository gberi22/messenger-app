package ge.ngvalia.messengerapp.ui.chat

import android.content.Context
import android.content.Intent
import android.util.Log

object ChatUtils {

    fun startChatActivity(
        context: Context,
        otherUserId: String,
        otherUserName: String,
        otherUserAvatar: String? = null
    ) {
        try {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("OTHER_USER_ID", otherUserId)
                putExtra("OTHER_USER_NAME", otherUserName)
                putExtra("OTHER_USER_AVATAR", otherUserAvatar)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ChatUtils", "Error starting chat activity", e)
            throw e
        }
    }
}