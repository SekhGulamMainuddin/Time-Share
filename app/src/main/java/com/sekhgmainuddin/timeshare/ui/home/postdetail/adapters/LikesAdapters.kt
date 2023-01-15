package com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.LikeWithProfile

class LikeAdapter(val context: Context): ListAdapter<LikeWithProfile, RecyclerView.ViewHolder>(LikeDiffCallBack()) {

    private class LikeDiffCallBack: DiffUtil.ItemCallback<LikeWithProfile>() {
        override fun areItemsTheSame(oldItem: LikeWithProfile, newItem: LikeWithProfile): Boolean {
            return oldItem.profileId == newItem.profileId
        }
        override fun areContentsTheSame(oldItem: LikeWithProfile, newItem: LikeWithProfile): Boolean {
            return oldItem==newItem
        }
    }

    class LikeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val profileImage= itemView.findViewById<ShapeableImageView>(R.id.likeProfileImage)
    }

    class LoadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType==1)
            LikeViewHolder(LayoutInflater.from(context).inflate(R.layout.liked_profiles_rv, parent, false))
        else LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.progress_rv_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (currentList[position].likedTime!=-1L){
            val item= currentList[position]
            Glide.with(context).load(item.profileImage).placeholder(R.drawable.default_profile_pic).into((holder as LikeViewHolder).profileImage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].likedTime==-1L) 0 else 1
    }

}