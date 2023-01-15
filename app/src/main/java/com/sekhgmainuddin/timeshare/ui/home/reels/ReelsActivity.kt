package com.sekhgmainuddin.timeshare.ui.home.reels

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.ActivityReelsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.reels.adapters.ReelsAdapter
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReelsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReelsBinding
    private val viewModel by viewModels<HomeViewModel>()
    private val reelsList= ArrayList<Reel>()

    private lateinit var adapter: ReelsAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private lateinit var reelCommentsFragment: ReelCommentsFragment
    private var recentLiked: Int= -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityReelsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initialize()
        bindObservers()

    }

    fun initialize(){

        viewModel.getReels()

        adapter = ReelsAdapter(this, arrayListOf("49ulF0YTsXPArQhKn5hAxEIAtSY21673447503555") ,object : ReelsAdapter.OnVideoPreparedListener {

            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }

            override fun reelLiked() {
                viewModel.likeReel(reelsList[binding.viewPager2.currentItem].reelId)
                recentLiked= binding.viewPager2.currentItem
                Toast.makeText(this@ReelsActivity, "Liked", Toast.LENGTH_SHORT).show()
            }

            override fun reelUnliked() {
                viewModel.likeReel(reelsList[binding.viewPager2.currentItem].reelId, true)
                Toast.makeText(this@ReelsActivity, "Removed Like", Toast.LENGTH_SHORT).show()
            }

            override fun openCommentDrawer(reel: Reel) {
                reelCommentsFragment= ReelCommentsFragment(reel)
                reelCommentsFragment.show(supportFragmentManager, "reel_comments_fragment")
            }

            override fun saveReel(reelId: String) {

            }

            override fun shareReel(reel: Reel) {

            }

        })

        binding.viewPager2.adapter = adapter

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                if (previousIndex != -1) {
                    val player = exoPlayerItems[previousIndex].exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }
                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if (newIndex != -1) {
                    val player = exoPlayerItems[newIndex].exoPlayer
                    player.playWhenReady = true
                    player.play()
                }
            }
        })
    }

    private fun bindObservers() {
        viewModel.newReels.observe(this){
            adapter.update(it)
            reelsList.addAll(it)
        }
        viewModel.likeReelStatus.observe(this){
            when(it){
                is NetworkResult.Success->{
                }
                is NetworkResult.Error->{
                    adapter.reels[recentLiked].likedAndCommentByMe--
                    binding.viewPager2.adapter?.notifyItemChanged(recentLiked)
                    Toast.makeText(this, "Failed to post the like", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading->{

                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }

}