package ge.ngvalia.messengerapp.util

import android.content.Context
import android.content.Intent
import android.util.Log
import ge.ngvalia.messengerapp.ui.chat.ChatActivity

object ChatUtils {

    fun startChatActivity(
        context: Context,
        otherUserId: String,
        otherUserName: String,
        otherUserAvatar: String? = null
    ) {
        try {

            val intent = Intent(context, ChatActivity::class.java).apply {

            }
            context.startActivity(intent)
        } catch (e: Exception) {
            throw e
        }
    }
}
