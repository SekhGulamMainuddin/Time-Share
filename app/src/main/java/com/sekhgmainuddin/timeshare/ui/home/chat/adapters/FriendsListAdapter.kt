package com.sekhgmainuddin.timeshare.ui.home.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FriendsLayoutRvBinding

class FriendsListAdapter(val context: Context, val userSelected : (Pair<String, User>) -> Unit): ListAdapter<Pair<String, User>, FriendsListAdapter.FriendsViewHolder>(FriendsDiffCallBack()) {

    private class FriendsDiffCallBack: DiffUtil.ItemCallback<Pair<String, User>>(){
        override fun areItemsTheSame(oldItem: Pair<String, User>, newItem: Pair<String, User>): Boolean {
            return oldItem.first==newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<String, User>, newItem: Pair<String, User>): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        return FriendsViewHolder(FriendsLayoutRvBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val item= currentList[position]
        holder.bind(item.second)
        holder.itemView.setOnClickListener {
            userSelected(item)
        }
    }

    class FriendsViewHolder(val binding: FriendsLayoutRvBinding, val context: Context): RecyclerView.ViewHolder(binding.root){
        fun bind(user: User){
            binding.apply {
                Glide.with(context).load(user.imageUrl).placeholder(R.drawable.default_profile_pic).into(profileImage)
                profileName.text= user.name
                profileBio.text= user.bio
                selectFriend.isVisible= user.isSelected
                selectFriend.isChecked= user.isSelected
            }
        }
    }

}