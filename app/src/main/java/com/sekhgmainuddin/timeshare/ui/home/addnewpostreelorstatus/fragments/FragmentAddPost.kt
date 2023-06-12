package com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.databinding.FragmentAddPostBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.adapters.ImageVideoViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.adapters.onClick
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.getFileExtension
import com.sekhgmainuddin.timeshare.utils.Utils.isImageOrVideo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddPost : Fragment(), onClick {

    private var _binding: FragmentAddPostBinding? = null
    private val binding: FragmentAddPostBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var adapter: ImageVideoViewPagerAdapter

    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private val imageVideoList = arrayListOf(PostImageVideo(imageOrVideo = -1))
    private val imageVideoUriList = arrayListOf<Uri>()
    private var currIndex = 0
    private var isAudioMuted = false
    private lateinit var progressDialog: Dialog
    private lateinit var intent: Intent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentAddPostBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        initialize()
        registerClickListeners()
        bindObserver()

    }

    private fun initialize() {

        Glide.with(requireContext()).load(viewModel.userData.value?.imageUrl).into(binding.profileImage)
        binding.profileName.text = viewModel.userData.value?.name

        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/*", "video/*")
        )
        intent.action = Intent.ACTION_GET_CONTENT

        adapter = ImageVideoViewPagerAdapter(
            requireContext(),
            this,
            object : ImageVideoViewPagerAdapter.OnVideoListener {
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

                }
            })
        binding.viewPagerImageVideo.adapter = adapter
        adapter.update(imageVideoList)

        TabLayoutMediator(binding.tabLayout, binding.viewPagerImageVideo)
        { _, _ -> }.attach()

        binding.viewPagerImageVideo.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
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
            if (imageVideoUriList.isNotEmpty()) {
                progressDialog.show()
                viewModel.addPost(imageVideoUriList, binding.descEditText.text.toString())
            }
        }

    }

    private fun bindObserver() {
        viewModel.addPostStatus.observe(viewLifecycleOwner) {
            if (imageVideoUriList.isNotEmpty()) {
                progressDialog.dismiss()
                when (it) {
                    is NetworkResult.Success -> {
                        if (it.data == true && it.statusCode == 200) {
                            Toast.makeText(
                                requireContext(),
                                "Post Uploaded Successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            findNavController().popBackStack()
                        }
                    }
                    is NetworkResult.Error -> {
                        if (it.statusCode == 500)
                            Toast.makeText(
                                requireContext(),
                                "Some error occurred while uploading the post",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                    is NetworkResult.Loading -> {
                        progressDialog.show()
                    }
                }
            }
        }
    }

    private val galleryLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { d->
        val it= d.data?.data
        if (it != null) {
            val isImageOrVideo =
                getFileExtension(it, requireContext())?.isImageOrVideo()
            if (isImageOrVideo == 0 || isImageOrVideo == 1) {
                imageVideoUriList.add(it)
                imageVideoList.add(
                    currIndex,
                    PostImageVideo(
                        isImageOrVideo,
                        if (isImageOrVideo == 0) it.toString() else "",
                        if (isImageOrVideo == 1) it.toString() else "",
                    )
                )
                currIndex++
                adapter.update(imageVideoList)
            } else {
                Toast.makeText(requireContext(), "File Format Not Supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewAddClick() {
        galleryLaunch.launch(intent)
    }

    override fun onClickToView(postImageVideoWithExoPlayer: PostImageVideo) {
        Toast.makeText(requireContext(), "Item Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()

        val index =
            exoPlayerItems.indexOfFirst { it.position == binding.viewPagerImageVideo.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index =
            exoPlayerItems.indexOfFirst { it.position == binding.viewPagerImageVideo.currentItem }
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