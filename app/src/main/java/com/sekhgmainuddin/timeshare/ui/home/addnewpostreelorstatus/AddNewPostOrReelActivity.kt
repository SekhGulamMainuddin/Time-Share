package com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.databinding.ActivityAddNewPostOrReelBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.adapters.ImageVideoViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.adapters.onClick
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.getFileExtension
import com.sekhgmainuddin.timeshare.utils.Utils.isImageOrVideo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNewPostOrReelActivity : AppCompatActivity(), onClick {

    private lateinit var binding: ActivityAddNewPostOrReelBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var adapter: ImageVideoViewPagerAdapter

    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    //    private val imageVideoUriList= mutableListOf<PostImageVideo>(PostImageVideo(-1,"",""))
    private val imageVideoUriList = mutableListOf<PostImageVideo>()
    private var currIndex = 0
    private var isAudioMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostOrReelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageVideoUriList.addAll(
            listOf(PostImageVideo(-1,"","")
//                PostImageVideo(
//                    1,
//                    "",
//                    "content://media/external_primary/video/media/1000028817",
//                ),
//                PostImageVideo(
//                    1,
//                    "",
//                    "content://media/external_primary/video/media/1000028816",
//                ),
//                PostImageVideo(
//                    1,
//                    "",
//                    "content://media/external_primary/video/media/1000028815",
//                )
            )
        )

        initialize()
        registerClickListeners()
        bindObserver()


    }

    private fun initialize() {

        adapter = ImageVideoViewPagerAdapter(this, this,  object : ImageVideoViewPagerAdapter.OnVideoListener {
            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }

            override fun onVideoClick(position: Int) {
                isAudioMuted = !isAudioMuted

                val index =
                    exoPlayerItems.indexOfFirst { it.position == binding.viewPagerImageVideo.currentItem }
                if (index != -1) {
                    val player = exoPlayerItems[index].exoPlayer
                    player.volume = if (isAudioMuted) 0f else 1f
                }

//                if (isAudioMuted) {
//                    binding.ivSpeaker.setImageResource(R.drawable.speaker_muted)
//                } else {
//                    binding.ivSpeaker.setImageResource(R.drawable.speaker_normal)
//                }
//
//                binding.ivSpeaker.visibility = View.VISIBLE
//                Handler(Looper.getMainLooper()).postDelayed({
//                    binding.ivSpeaker.visibility = View.GONE
//                }, 1000)
            }
        })
        binding.viewPagerImageVideo.adapter = adapter
        adapter.update(imageVideoUriList)

        TabLayoutMediator(binding.tabLayout, binding.viewPagerImageVideo)
        { _, _ -> }.attach()

        binding.viewPagerImageVideo.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
                    player.volume = if (isAudioMuted) 0f else 1f
                    player.play()
                }
            }
        })

    }

    private fun registerClickListeners() {

        binding.postButton.setOnClickListener {
//            viewModel.addPost(imageVideoUriList, "Hello this is a testing post")
        }


    }

    private fun bindObserver() {
        viewModel.addPostStatus.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == true && it.statusCode == 200)
                        Toast.makeText(this, "Post Uploaded Successfully", Toast.LENGTH_SHORT)
                            .show()
                }
                is NetworkResult.Error -> {
                    if (it.statusCode == 500)
                        Toast.makeText(
                            this,
                            "Some error occurred while uploading the post",
                            Toast.LENGTH_SHORT
                        ).show()
                }
                is NetworkResult.Loading -> {

                }
            }
        }
    }

    private val galleryLaunch = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            Log.d("selectedVideos", "->  $it ")
            val isImageOrVideo =
                getFileExtension(it, this@AddNewPostOrReelActivity)?.isImageOrVideo()
            if (isImageOrVideo == 0 || isImageOrVideo == 1) {
                imageVideoUriList.add(
                    currIndex,
                    PostImageVideo(
                        isImageOrVideo,
                        if (isImageOrVideo == 0) it.toString() else "",
                        if (isImageOrVideo == 1) it.toString() else "",
                    )
                )
                currIndex++
                adapter.update(imageVideoUriList)
            } else {
                Toast.makeText(this, "File Format Not Supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewAddClick() {
        galleryLaunch.launch("image/* video/*")
    }

    override fun onClickToView(postImageVideoWithExoPlayer: PostImageVideo) {
        Toast.makeText(this, "Item Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPagerImageVideo.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPagerImageVideo.currentItem }
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