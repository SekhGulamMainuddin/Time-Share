package com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.CommentWithProfile
import com.sekhgmainuddin.timeshare.databinding.ProgressRvLayoutBinding

class CommentAdapter(val context: Context): ListAdapter<CommentWithProfile, RecyclerView.ViewHolder>(CommentDiffCallBack()) {

    private class CommentDiffCallBack: DiffUtil.ItemCallback<CommentWithProfile>() {
        override fun areItemsTheSame(oldItem: CommentWithProfile, newItem: CommentWithProfile): Boolean {
            return oldItem.profileId == newItem.profileId
        }
        override fun areContentsTheSame(oldItem: CommentWithProfile, newItem: CommentWithProfile): Boolean {
            return oldItem==newItem
        }
    }

    class CommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val profileImage= itemView.findViewById<ShapeableImageView>(R.id.addCommentProfileImage)
        val profileName= itemView.findViewById<TextView>(R.id.profileName)
        val commentTextView= itemView.findViewById<TextView>(R.id.commentTextView)
    }

    class LoadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType==1)
            CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.post_detail_comment_layout_rv, parent, false))
        else LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.progress_rv_layout, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return if(currentList[position].commentTime==-1L) 0 else 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (currentList[position].commentTime!=-1L){
            val item= currentList[position]
            val viewHolder= holder as CommentViewHolder
            Glide.with(context).load(item.profileImage).placeholder(R.drawable.default_profile_pic).into(holder.profileImage)
            viewHolder.commentTextView.text= item.comment
            viewHolder.profileName.text= item.profileName
        }
    }

}