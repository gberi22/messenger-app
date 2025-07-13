package ge.ngvalia.messengerapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import ge.ngvalia.messengerapp.R

class ConversationAdapter(private val conversations: List<Conversation>) :
    RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userAvatar: ShapeableImageView = view.findViewById(R.id.user_avatar)
        val userName: TextView = view.findViewById(R.id.user_name)
        val lastMessage: TextView = view.findViewById(R.id.last_message)
        val messageTime: TextView = view.findViewById(R.id.message_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]

        holder.userName.text = conversation.userName
        holder.lastMessage.text = conversation.lastMessage
        holder.messageTime.text = conversation.timestamp

        // Set placeholder avatar (you can use actual images later)
        holder.userAvatar.setImageResource(R.drawable.ic_person)
    }

    override fun getItemCount() = conversations.size
}
