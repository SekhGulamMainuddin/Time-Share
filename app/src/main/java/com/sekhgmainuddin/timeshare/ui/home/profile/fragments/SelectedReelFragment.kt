package com.sekhgmainuddin.timeshare.ui.home.profile.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.SelectedReelFromProfileBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedReelFragment : DialogFragment() {

    private var _binding: SelectedReelFromProfileBinding? = null
    private val binding: SelectedReelFromProfileBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private var reel: Reel? = null
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        _binding = SelectedReelFromProfileBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reel = viewModel.reelPassedToView.value
        initialize()

    }

    private fun initialize() {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
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
        reel?.let { rl->
            binding.apply {
                reelViews.text= rl.reelViewsCount.toString()
                playerView.player = exoPlayer
                playerView.setOnClickListener {
                    val bundle= Bundle()
                    bundle.putBoolean("showUserReels", true)
                    bundle.putString("showReelId", rl.reelId)
                    findNavController().navigate(R.id.reelsFragment, bundle)
                }
                exoPlayer.seekTo(0)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                val dataSourceFactory = DefaultDataSource.Factory(requireContext())

                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(rl.reelUrl))

                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()

                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }
}