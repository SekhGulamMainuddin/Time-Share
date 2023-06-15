package com.sekhgmainuddin.timeshare.ui.home.profile.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.databinding.PostLayoutInProfileBinding

class ProfilePostsAdapter(val context: Context, val selectedPost: (Post, Boolean) -> (Unit)) : ListAdapter<Post, ProfilePostsAdapter.ProfilePostsViewHolder>(ProfilePostDiffCallBack()) {

    private class ProfilePostDiffCallBack(): DiffUtil.ItemCallback<Post>(){
        override fun areItemsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem==newItem
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        return ProfilePostsViewHolder(PostLayoutInProfileBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        holder.isVideo.isVisible= false
        var imageUrl= ""
        for (i in currentList[position].postContent!!){
            if (i.imageUrl?.isNotEmpty() == true){
                imageUrl= i.imageUrl
                break
            }
            if (i.thumbnail.isNotEmpty()) {
                imageUrl = i.thumbnail
                holder.isVideo.isVisible= true
                break
            }
        }
        holder.initialize(imageUrl)
        holder.itemView.setOnLongClickListener {
            selectedPost.invoke(currentList[position], true)
            true
        }
        holder.itemView.setOnClickListener {
            selectedPost.invoke(currentList[position], false)
        }
    }

    class ProfilePostsViewHolder(val binding: PostLayoutInProfileBinding, val context: Context): RecyclerView.ViewHolder(binding.root){

        val isVideo = binding.isVideo

        fun initialize(postImageUrl: String){
            Glide.with(context).load(postImageUrl).addListener(object: RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Glide.with(context).load(R.drawable.some_error_occurred_image).into(binding.postImage)
                    binding.progressCircular.visibility= View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressCircular.visibility= View.GONE
                    return false
                }

            }).into(binding.postImage)
        }
    }

}