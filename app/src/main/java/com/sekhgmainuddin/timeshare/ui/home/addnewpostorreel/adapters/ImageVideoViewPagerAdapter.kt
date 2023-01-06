package com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo

class ImageVideoViewPagerAdapter(val context: Context, val onclick: onClick): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val list= ArrayList<PostImageVideo>()
//    private val exoPlayer= ExoPlayer.Builder(context).build()

    fun update(newList: List<PostImageVideo>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    class ImageVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage= itemView.findViewById<ImageView>(R.id.postImage)
        val playerView= itemView.findViewById<StyledPlayerView>(R.id.playerView)
    }

    class EmptyTempImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutTemp= itemView.findViewById<LinearLayout>(R.id.layoutTemp)
        val addPostButton= itemView.findViewById<ImageButton>(R.id.addPostButton)
        val addPostTV= itemView.findViewById<TextView>(R.id.addPostTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType==-1)
            EmptyTempImageViewHolder(LayoutInflater.from(context).inflate(R.layout.empty_post_layout, parent, false))
        else
            ImageVideoViewHolder(LayoutInflater.from(context).inflate(R.layout.posts_view_pager_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item= list[position]
        if (item.imageOrVideo==-1) {
            val emptyTempHolder= holder as EmptyTempImageViewHolder
            emptyTempHolder.layoutTemp.setOnClickListener {
                onclick.onNewAddClick()
            }
            emptyTempHolder.addPostTV.setOnClickListener {
                onclick.onNewAddClick()
            }
            emptyTempHolder.addPostButton.setOnClickListener {
                onclick.onNewAddClick()
            }
        }
        else{
            val imageViewHolder= holder as ImageVideoViewHolder
            imageViewHolder.postImage.visibility= View.INVISIBLE
            imageViewHolder.playerView.visibility= View.INVISIBLE
            imageViewHolder.itemView.setOnClickListener {
                onclick.onClickToView(item)
            }
            if (item.imageOrVideo==0) {
                imageViewHolder.postImage.visibility= View.VISIBLE
                Glide.with(context).load(item.imageUrl).placeholder(R.drawable.default_profile_pic)
                    .into(imageViewHolder.postImage)
            }
            else {
                imageViewHolder.playerView.visibility= View.VISIBLE
//                imageViewHolder.playerView.player = exoPlayer
//                val mediaItem: MediaItem = MediaItem.fromUri(item.videoUrl!!)
//                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.prepare()
//                exoPlayer.play()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].imageOrVideo==-1)
            -1
        else {
            list[position].imageOrVideo
        }
    }

    override fun getItemCount(): Int {
        return list.size
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

interface onClick{
    fun onNewAddClick()
    fun onClickToView(postImageVideo: PostImageVideo)
}