package com.sekhgmainuddin.timeshare.ui.home.reels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.FragmentReelsHomeBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.reels.adapters.ReelsAdapter
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReelsFragment : Fragment() {

    private var _binding: FragmentReelsHomeBinding?= null
    private val binding: FragmentReelsHomeBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private val reelsList= ArrayList<String>()

    private lateinit var adapter: ReelsAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private lateinit var reelCommentsFragment: ReelCommentsFragment
    private var recentLiked: Int= -1
    private var showUserReels= false
    private var showReelId: String?= ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentReelsHomeBinding.inflate(inflater)
        showUserReels = arguments?.getBoolean("showUserReels", false) == true
        showReelId = arguments?.getString("showReelId")
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        bindObservers()

    }

    private fun initialize(){

        adapter = ReelsAdapter(requireContext(), arrayListOf("49ulF0YTsXPArQhKn5hAxEIAtSY21673447503555") ,object : ReelsAdapter.OnVideoPreparedListener {

            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }

            override fun reelLiked() {
                viewModel.likeReel(reelsList[binding.viewPager2.currentItem])
                recentLiked= binding.viewPager2.currentItem
                Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
            }

            override fun reelUnliked() {
                viewModel.likeReel(reelsList[binding.viewPager2.currentItem], true)
                Toast.makeText(requireContext(), "Removed Like", Toast.LENGTH_SHORT).show()
            }

            override fun openCommentDrawer(reel: Reel) {
                reelCommentsFragment= ReelCommentsFragment(reel)
                reelCommentsFragment.show(childFragmentManager, "reel_comments_fragment")
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

        if (showUserReels) {
            viewModel.reelPassedToView.value?.let {
                adapter.update(arrayListOf(it))
                reelsList.add(it.reelId)
            }
            if (viewModel.reelPassedToView.value==null)
                viewModel.getReels(showUserReels = true)
        }
        else {
            viewModel.getReels()
        }

    }

    private fun bindObservers() {
        viewModel.newReels.observe(viewLifecycleOwner){ newList->
            adapter.update(newList)
            newList.forEach {
                reelsList.add(it.reelId)
            }
        }
        viewModel.likeReelStatus.observe(viewLifecycleOwner){
            when(it){
                is NetworkResult.Success->{
                }
                is NetworkResult.Error->{
                    adapter.reels[recentLiked].likedAndCommentByMe--
                    binding.viewPager2.adapter?.notifyItemChanged(recentLiked)
                    Toast.makeText(requireContext(), "Failed to post the like", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }

}