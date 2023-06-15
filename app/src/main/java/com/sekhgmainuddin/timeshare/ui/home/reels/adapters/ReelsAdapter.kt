package com.sekhgmainuddin.timeshare.ui.home.reels.adapters

import DoubleClickListener
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.ReelsLayoutRvBinding
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo

class ReelsAdapter(
    var context: Context,
    var savedReels: ArrayList<String>,
    var videoListener: OnVideoPreparedListener
) : RecyclerView.Adapter<ReelsAdapter.ReelsViewHolder>() {

    val reels= ArrayList<Reel>()

    fun update(newReels: ArrayList<Reel>){
        reels.addAll(newReels)
        notifyDataSetChanged()
    }

    class ReelsViewHolder(
        val binding: ReelsLayoutRvBinding,
        var context: Context,
        var videoPreparedListener: OnVideoPreparedListener
    ) : RecyclerView.ViewHolder(binding.root) {

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
                        binding.pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            binding.playerView.player = exoPlayer

            binding.playerView.setOnClickListener(object: DoubleClickListener(){
                override fun onSingleClick(v: View?) {
                    val drawablePlayOrPause: Int
                    if (exoPlayer.isPlaying){
                        exoPlayer.pause()
                        drawablePlayOrPause= R.drawable.ic_baseline_pause_24
                    }else{
                        exoPlayer.play()
                        drawablePlayOrPause= R.drawable.ic_baseline_play_arrow_24
                    }
                    binding.playPauseImage.setImageDrawable(AppCompatResources.getDrawable(context, drawablePlayOrPause))
                    binding.playPauseImage.isVisible= true
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.playPauseImage.isVisible= false
                    },1000)
                }

                override fun onDoubleClick(v: View?) {
                    videoPreparedListener.reelLiked()
                    showAnimation(binding, context)
                }

            })

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelsViewHolder {
        val view = ReelsLayoutRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ReelsViewHolder(view, context, videoListener)
    }

    override fun onBindViewHolder(holder: ReelsViewHolder, position: Int) {
        val item = reels[position]

        holder.binding.tvTitle.text = item.reelDesc
        holder.binding.tvTitle.isSelected= true
        holder.setVideoPath(Uri.parse(item.reelUrl))
        holder.binding.likeButton.setImageDrawable(AppCompatResources.getDrawable(context,if (item.likedAndCommentByMe in intArrayOf(1,3)) R.drawable.liked_icon else R.drawable.love_icon))
        if (item.likedAndCommentByMe !in intArrayOf(1,3))
            holder.binding.likeButton.imageTintList= ColorStateList.valueOf(context.getColor(R.color.white))
        holder.binding.commentButton.imageTintList= ColorStateList.valueOf(context.getColor(if (item.likedAndCommentByMe in intArrayOf(2,3)) R.color.orangePink else R.color.white))
        holder.binding.savePostButton.setImageDrawable(AppCompatResources.getDrawable(context, if (savedReels.contains(item.reelId)) R.drawable.ic_baseline_bookmark_added_24 else R.drawable.save_post_icon))
        holder.binding.savePostButton.imageTintList= ColorStateList.valueOf(context.getColor(if(savedReels.contains(item.reelId)) R.color.golden_color else R.color.white))
        holder.binding.likeCount.text= item.likeCount.toString()
        holder.binding.commentCount.text= item.commentCount.toString()
        holder.binding.reelsPostTime.text= item.reelPostTime.getTimeAgo()
        Glide.with(context).load(item.creatorImageUrl).placeholder(R.drawable.default_profile_pic).into(holder.binding.creatorImage)
        holder.binding.creatorName.text= item.creatorName
        holder.binding.commentButton.setOnClickListener {
            videoListener.openCommentDrawer(item)
        }
        holder.binding.savePostButton.setOnClickListener {
            videoListener.saveReel(item.reelId)
        }
        holder.binding.shareButton.setOnClickListener {
            videoListener.shareReel(item)
        }
        holder.binding.likeButton.setOnClickListener {
            if (item.likedAndCommentByMe !in intArrayOf(1,3)){
                reels[position].likedAndCommentByMe++
                showAnimation(holder.binding, context)
                videoListener.reelLiked()
                holder.binding.likeCount.text= "${item.likeCount+1}"
            }else{
                reels[position].likedAndCommentByMe--
                holder.binding.likeButton.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.love_icon))
                holder.binding.likeButton.imageTintList= ColorStateList.valueOf(context.getColor(R.color.white))
                videoListener.reelUnliked()
                holder.binding.likeCount.text= "${item.likeCount-1}"
            }
        }
    }

    override fun getItemCount(): Int {
        return reels.size
    }

    companion object{
        fun showAnimation(binding: ReelsLayoutRvBinding, context: Context) {
            binding.likeAnimation.isVisible = true
            binding.likeAnimation.playAnimation()
            binding.likeButton.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.liked_icon
                )
            )
            binding.likeButton.imageTintList = null
            Handler(Looper.getMainLooper()).postDelayed({
                binding.likeAnimation.cancelAnimation()
                binding.likeAnimation.isVisible = false
            }, 750)
        }
    }

    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
        fun reelLiked()
        fun reelUnliked()
        fun openCommentDrawer(reel: Reel)
        fun saveReel(reelId :String)
        fun shareReel(reel: Reel)

    }
}