package com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo

class ImageVideoViewPagerAdapter(val context: Context, val onclick: onClick, var videoListener: OnVideoListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val list= ArrayList<PostImageVideo>()

    fun update(newList: List<PostImageVideo>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    class ImageVideoViewHolder(itemView: View, val context: Context, var videoPreparedListener: OnVideoListener) : RecyclerView.ViewHolder(itemView) {
        val postImage= itemView.findViewById<ImageView>(R.id.postImage)
        val playerView= itemView.findViewById<StyledPlayerView>(R.id.playerView)
        val progressBar= itemView.findViewById<ProgressBar>(R.id.pbLoading)
        private lateinit var exoPlayer: ExoPlayer
        private lateinit var mediaSource: MediaSource

        fun setVideoPath(uri: Uri) {

            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(context, "Can't play this video", Toast.LENGTH_SHORT).show()
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        progressBar.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        progressBar.visibility = View.GONE
                    }
                }
            })

            playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(context)

            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(uri))

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }

            videoPreparedListener.onVideoPrepared(ExoPlayerItem(exoPlayer, absoluteAdapterPosition))
        }
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
            ImageVideoViewHolder(LayoutInflater.from(context).inflate(R.layout.posts_view_pager_layout, parent, false), context, videoListener)
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
                imageViewHolder.setVideoPath(Uri.parse(item.videoUrl))

                imageViewHolder.itemView.setOnClickListener {
                    videoListener.onVideoClick(position)
                }
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

    interface OnVideoListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
        fun onVideoClick(position: Int)
    }

}

interface onClick{
    fun onNewAddClick()
    fun onClickToView(postImageVideoWithExoPlayer: PostImageVideo)
}