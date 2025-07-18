package ge.ngvalia.messengerapp.ui.homepage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ge.ngvalia.messengerapp.R

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.user_avatar)
        private val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val messageTextView: TextView = itemView.findViewById(R.id.last_message)
        private val timeTextView: TextView = itemView.findViewById(R.id.message_time)

        fun bind(conversation: Conversation) {
            nameTextView.text = conversation.otherUserName
            messageTextView.text = if (conversation.lastMessage.isNotEmpty()) {
                conversation.lastMessage
            } else {
                "No messages yet"
            }
            timeTextView.text = conversation.lastMessageTime

            if (conversation.otherUserAvatar.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(conversation.otherUserAvatar)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.ic_error)
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.person)
            }

            itemView.setOnClickListener {
                onConversationClick(conversation)
            }

            if (conversation.isUnread) {
                nameTextView.setTypeface(nameTextView.typeface, android.graphics.Typeface.BOLD)
                messageTextView.setTypeface(messageTextView.typeface, android.graphics.Typeface.BOLD)
            } else {
                nameTextView.setTypeface(nameTextView.typeface, android.graphics.Typeface.NORMAL)
                messageTextView.setTypeface(messageTextView.typeface, android.graphics.Typeface.NORMAL)
            }
        }
    }

    class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.chatRoomId == newItem.chatRoomId
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
}
