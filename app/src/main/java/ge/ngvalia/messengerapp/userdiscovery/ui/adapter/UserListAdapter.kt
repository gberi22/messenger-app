package ge.ngvalia.messengerapp.userdiscovery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ge.ngvalia.messengerapp.databinding.ItemUserBinding
import com.bumptech.glide.Glide
import ge.ngvalia.messengerapp.data.model.User

class UserListAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(old: User, new: User) = old.nickname == new.nickname
            override fun areContentsTheSame(old: User, new: User) = old == new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.userName.text = user.nickname
            binding.profession.text = user.profession
            Glide.with(binding.userAvatar).load(user.profilePicUrl).into(binding.userAvatar)

            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}