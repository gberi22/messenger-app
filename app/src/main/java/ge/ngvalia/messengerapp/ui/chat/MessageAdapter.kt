package ge.ngvalia.messengerapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import ge.ngvalia.messengerapp.R
import ge.ngvalia.messengerapp.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = listOf<Message>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            messageText.text = message.text
            messageTime.text = formatTime(message.timestamp)
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            messageText.text = message.text
            messageTime.text = formatTime(message.timestamp)
        }
    }

    companion object {
        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
                diff < 24 * 60 * 60 * 1000 -> {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                }
                else -> {
                    SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
                }
            }
        }
    }
}
