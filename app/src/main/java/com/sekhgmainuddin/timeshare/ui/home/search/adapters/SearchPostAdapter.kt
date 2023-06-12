package com.sekhgmainuddin.timeshare.ui.home.search.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.sekhgmainuddin.timeshare.databinding.SearchPostLayoutRvBinding
import com.sekhgmainuddin.timeshare.ui.home.search.layoutmanager.SpanSize

class SearchPostAdapter(val context: Context, val selectedPost: (Post) -> (Unit), val instantView: (Post) -> (Unit)) : ListAdapter<Pair<Post, String>, SearchPostAdapter.SearchPostViewHolder>(ProfilePostDiffCallBack()) {

    private class ProfilePostDiffCallBack(): DiffUtil.ItemCallback<Pair<Post, String>>(){
        override fun areItemsTheSame(
            oldItem: Pair<Post, String>,
            newItem: Pair<Post, String>
        ): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: Pair<Post, String>,
            newItem: Pair<Post, String>
        ): Boolean {
            return oldItem.first==newItem.first && oldItem.second==newItem.second
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPostViewHolder {
        return SearchPostViewHolder(SearchPostLayoutRvBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: SearchPostViewHolder, position: Int) {
        holder.initialize(currentList[position].second)
        holder.itemView.setOnClickListener{
            selectedPost.invoke(currentList[position].first)
        }
        holder.itemView.setOnLongClickListener {
            instantView.invoke(currentList[position].first)
            true
        }
    }

    class SearchPostViewHolder(val binding: SearchPostLayoutRvBinding, val context: Context): RecyclerView.ViewHolder(binding.root){
        fun initialize(postImageUrl: String){

            Glide.with(context).load(postImageUrl).addListener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
//                    Glide.with(context).load(R.drawable.some_error_occurred_image).into(binding.postImage)
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