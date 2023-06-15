package com.sekhgmainuddin.timeshare.ui.home.newuserfollow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FollowUserRvBinding

class UserFollowAdapter(val context: Context, val followClicked : (String) -> Unit, val unFollowClicked : (String) -> Unit) :
    RecyclerView.Adapter<UserFollowAdapter.UserFollowViewHolder>() {

    val following = ArrayList<String>()
    val list = ArrayList<User>()

    fun updateList(userList: List<User>) {
        list.clear()
        list.addAll(userList)
        notifyDataSetChanged()
    }

    class UserFollowViewHolder(val binding: FollowUserRvBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        val followButton = binding.followButton
        fun bind(user: User) {
            binding.apply {
                Glide.with(context).load(user.imageUrl).placeholder(R.drawable.default_profile_pic)
                    .into(profileImage)
                profileName.text = user.name
                profileBio.text = user.bio
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFollowViewHolder {
        return UserFollowViewHolder(
            FollowUserRvBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            ), context
        )
    }

    override fun onBindViewHolder(holder: UserFollowViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
        if (!following.contains(item.userId)) {
            holder.followButton.text = context.getString(R.string.follow)
            holder.followButton.setTextColor(context.getColor(R.color.red))
            holder.followButton.setOnClickListener {
                followClicked.invoke(item.userId)
                following.add(item.userId)
                notifyItemChanged(position)
            }
        } else {
            holder.followButton.text = context.getString(R.string.un_follow)
            holder.followButton.setTextColor(context.getColor(R.color.phone_blue))
            holder.followButton.setOnClickListener {
                unFollowClicked.invoke(item.userId)
                following.remove(item.userId)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}