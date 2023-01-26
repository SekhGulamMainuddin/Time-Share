package com.sekhgmainuddin.timeshare.ui.home.search.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.SearchAccountLayoutRvBinding

class SearchAccountAdapter(val context: Context, val accountClicked: (User) -> Unit): ListAdapter<Pair<User, String?>, SearchAccountAdapter.SearchAccountViewHolder>(AccountDiffCallback()) {

    private class AccountDiffCallback: DiffUtil.ItemCallback<Pair<User, String?>>(){
        override fun areItemsTheSame(
            oldItem: Pair<User, String?>,
            newItem: Pair<User, String?>
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Pair<User, String?>,
            newItem: Pair<User, String?>
        ): Boolean {
            return oldItem.first == newItem.first
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAccountViewHolder {
        return SearchAccountViewHolder(SearchAccountLayoutRvBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: SearchAccountViewHolder, position: Int) {
        val user= currentList[position]
        holder.loadViews(user)
        holder.itemView.setOnClickListener {
            accountClicked.invoke(user.first)
        }
    }

    class SearchAccountViewHolder(val binding: SearchAccountLayoutRvBinding, val context: Context): RecyclerView.ViewHolder(binding.root){
        fun loadViews(details: Pair<User, String?>){
            val user= details.first
            val mutualFollowers= details.second
            binding.apply {
                Glide.with(context).load(user.imageUrl).placeholder(R.drawable.default_profile_pic).into(profileImage)
                profileName.text= user.name
                profileBio.text= user.bio
                profileLocation.text= user.location
                followedBy.isVisible= !mutualFollowers.isNullOrEmpty()
                followedBy.text= mutualFollowers.toString()
                Log.d("searchQuery", "getProfileFromSearch: $mutualFollowers")
            }
        }
    }

}