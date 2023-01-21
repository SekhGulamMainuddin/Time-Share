package com.sekhgmainuddin.timeshare.ui.home.profile.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.PostLayoutInProfileBinding

class ProfilePostsAdapter(val context: Context) : ListAdapter<Pair<String, String>, ProfilePostsAdapter.ProfilePostsViewHolder>(ProfilePostDiffCallBack()) {

    private class ProfilePostDiffCallBack(): DiffUtil.ItemCallback<Pair<String, String>>(){
        override fun areItemsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            return oldItem.first==newItem.first && oldItem.second==newItem.second
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        return ProfilePostsViewHolder(PostLayoutInProfileBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        holder.initialize(currentList[position].second)
    }

    class ProfilePostsViewHolder(val binding: PostLayoutInProfileBinding, val context: Context): RecyclerView.ViewHolder(binding.root){
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