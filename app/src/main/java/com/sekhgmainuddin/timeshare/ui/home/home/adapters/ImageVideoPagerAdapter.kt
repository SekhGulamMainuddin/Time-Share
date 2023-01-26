package com.sekhgmainuddin.timeshare.ui.home.home.adapters

import DoubleClickListener
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity

class ImageVideoViewPagerAdapter(val context: Context, val post: PostEntity, val onclick: (Int)->(Unit)) :
    RecyclerView.Adapter<ImageVideoViewPagerAdapter.ImageVideoViewHolder>() {

    class ImageVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage = itemView.findViewById<ImageView>(R.id.postImage)
        val playerView = itemView.findViewById<StyledPlayerView>(R.id.playerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVideoViewHolder {
        return ImageVideoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.posts_view_pager_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageVideoViewHolder, position: Int) {
        val item = post.postContent!![position]
        val listener= object : DoubleClickListener(){
            override fun onSingleClick(v: View?) {
                onclick(1)
            }

            override fun onDoubleClick(v: View?) {
                onclick(2)
            }
        }

        holder.postImage.visibility = View.INVISIBLE
        holder.playerView.visibility = View.INVISIBLE

        if (item.imageOrVideo == 0) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(context).load(item.imageUrl).placeholder(R.drawable.white_image)
                .into(holder.postImage)

            holder.postImage.setOnClickListener(listener)
        } else {
            holder.playerView.visibility = View.VISIBLE
            holder.playerView.setOnClickListener(listener)
//                imageViewHolder.playerView.player = exoPlayer
//                val mediaItem: MediaItem = MediaItem.fromUri(item.videoUrl!!)
//                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.prepare()
//                exoPlayer.play()
        }

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

//    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
//        exoPlayer.pause()
//        super.onViewDetachedFromWindow(holder)
//    }
//
//    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
//        val position= holder.bindingAdapterPosition
//        val mediaItem: MediaItem = MediaItem.fromUri(list[position].videoUrl!!)
//        exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.prepare()
//        exoPlayer.play()
//        super.onViewAttachedToWindow(holder)
//    }

}