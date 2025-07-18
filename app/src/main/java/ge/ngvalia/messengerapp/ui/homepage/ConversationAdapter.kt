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

            // Load avatar
            if (conversation.otherUserAvatar.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(conversation.otherUserAvatar)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_error)
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.ic_person)
            }

            // Show/hide unread indicator

            // Set click listener
            itemView.setOnClickListener {
                onConversationClick(conversation)
            }

            // Style unread messages differently
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

//// Search Results Adapter for new users
//class SearchResultsAdapter(
//    private val onUserClick: (Map<String, Any>) -> Unit
//) : ListAdapter<Map<String, Any>, SearchResultsAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_conversation, parent, false)
//        return SearchResultViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val avatarImageView: ImageView = itemView.findViewById(R.id.user_avatar)
//        private val nameTextView: TextView = itemView.findViewById(R.id.user_name)
//        private val professionTextView: TextView = itemView.findViewById(R.id.profession_text_view)
//
//        fun bind(user: Map<String, Any>) {
//            val nickname = user["nickname"] as? String ?: "Unknown User"
//            val profilePicUrl = user["profilePicUrl"] as? String ?: ""
//            val profession = user["profession"] as? String ?: ""
//
//            nameTextView.text = nickname
//            professionTextView.text = profession
//            professionTextView.visibility = if (profession.isNotEmpty()) View.VISIBLE else View.GONE
//
//            // Load avatar
//            if (profilePicUrl.isNotEmpty()) {
//                Glide.with(itemView.context)
//                    .load(profilePicUrl)
//                    .transform(CircleCrop())
//                    .placeholder(R.drawable.ic_person_placeholder)
//                    .error(R.drawable.ic_person_placeholder)
//                    .into(avatarImageView)
//            } else {
//                avatarImageView.setImageResource(R.drawable.ic_person_placeholder)
//            }
//
//            itemView.setOnClickListener {
//                onUserClick(user)
//            }
//        }
//    }
//
//    class SearchResultDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
//        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
//            return oldItem["uid"] == newItem["uid"]
//        }
//
//        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
//            return oldItem == newItem
//        }
//    }
//}