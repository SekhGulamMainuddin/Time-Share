package com.sekhgmainuddin.timeshare.ui.home.home.adapters

import DoubleClickListener
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.databinding.PostsViewPagerLayoutBinding

class ImageVideoViewPagerAdapter(
    val context: Context,
    val post: PostEntity,
    val onclick: (Int) -> (Unit)
) :
    RecyclerView.Adapter<ImageVideoViewPagerAdapter.ImageVideoViewHolder>() {

    class ImageVideoViewHolder(val binding: PostsViewPagerLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.playerView.isVisible = false
            binding.pbLoading.isVisible = false
        }

        val postImage = binding.postImage
        val isVideo = binding.isVideoPlayButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVideoViewHolder {
        return ImageVideoViewHolder(
            PostsViewPagerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageVideoViewHolder, position: Int) {
        val item = post.postContent!![position]
        val listener = object : DoubleClickListener() {
            override fun onSingleClick(v: View?) {
                onclick(1)
            }

            override fun onDoubleClick(v: View?) {
                onclick(2)
            }
        }
        holder.postImage.setOnClickListener(listener)
        holder.isVideo.isVisible = item.imageOrVideo == 1
        Glide.with(context).load(if (item.imageOrVideo == 0) item.imageUrl else item.thumbnail)
            .placeholder(R.drawable.white_image)
            .into(holder.postImage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (post.postContent!![position].imageOrVideo == -1)
            -1
        else {
            post.postContent[position].imageOrVideo
        }
    }

    override fun getItemCount(): Int {
        return post.postContent!!.size
    }

}