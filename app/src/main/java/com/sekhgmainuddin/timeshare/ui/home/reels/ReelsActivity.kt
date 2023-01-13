package com.sekhgmainuddin.timeshare.ui.home.reels

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.ActivityReelsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReelsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReelsBinding
    private val viewModel by viewModels<HomeViewModel>()
    private val reelsList= ArrayList<Reel>()

    private lateinit var adapter: ReelsAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityReelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getReels()


        adapter = ReelsAdapter(this, arrayListOf("49ulF0YTsXPArQhKn5hAxEIAtSY21673447503555") ,object : ReelsAdapter.OnVideoPreparedListener {

            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }

            override fun reelsLiked() {
                Log.d("reelsLiked", "reelsLiked: ${reelsList[binding.viewPager2.currentItem]}")
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

        bindObservers()

    }

    private fun bindObservers() {
        viewModel.newReels.observe(this){
            adapter.update(it)
            reelsList.addAll(it)
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