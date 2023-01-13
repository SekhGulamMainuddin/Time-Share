package com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters

import android.content.Context
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

class CommentAdapter(val context: Context): ListAdapter<CommentWithProfile, CommentAdapter.CommentViewHolder>(CommentDiffCallBack()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.post_detail_comment_layout_rv, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item= currentList[position]
        Glide.with(context).load(item.profileImage).placeholder(R.drawable.default_profile_pic).into(holder.profileImage)
        holder.commentTextView.text= item.comment
        holder.profileName.text= item.profileName
    }

}