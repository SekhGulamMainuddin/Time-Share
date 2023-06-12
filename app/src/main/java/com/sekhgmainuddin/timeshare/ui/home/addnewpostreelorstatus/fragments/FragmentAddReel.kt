package com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentAddReelBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.utils.Utils.hideKeyboard
import com.sekhgmainuddin.timeshare.utils.Utils.showKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddReel : Fragment() {

    private var _binding: FragmentAddReelBinding? = null
    private val binding: FragmentAddReelBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private var exoPlayer: ExoPlayer? = null
    private var videoUri: Uri? = null
    private var captionText_ = "Reel"
    private var showingCaptionEditText = false
    private lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReelBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        registerListeners()
        bindObservers()

    }

    private fun registerListeners() {
        binding.apply {
            exoPlayer = ExoPlayer.Builder(requireActivity().applicationContext).build()
            playerView.player = exoPlayer
            exoPlayer?.seekTo(0)
            exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
            addReelsButton.shrink()
            uploadReelButton.isVisible = false
            addCaption.isVisible = false
            Glide.with(requireContext()).load(viewModel.userData.value?.imageUrl).into(creatorImage)
            creatorName.text = viewModel.userData.value?.name
            addReelsButton.setOnClickListener {
                if (uploadReelButton.isVisible) {
                    addReelsButton.shrink()
                    uploadReelButton.isVisible = false
                    addCaption.isVisible = false
                } else {
                    addReelsButton.extend()
                    uploadReelButton.isVisible = true
                    addCaption.isVisible = true
                }
            }
            uploadReelButton.setOnClickListener {
                if (videoUri == null) {
                    reelVideo.launch("video/mp4")
                    uploadReelButton.setImageResource(R.drawable.send_icon)
                } else if (captionText_.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Add Some Description or Caption for the reel",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    progressDialog.show()
                    viewModel.uploadReel(videoUri!!, captionText_)
                }
            }
            removeReelButton.setOnClickListener {
                videoUri= null
                exoPlayer?.pause()
                exoPlayer?.clearMediaItems()
                uploadReelButton.setImageResource(R.drawable.baseline_add_24)
                Toast.makeText(requireContext(), "Video Removed", Toast.LENGTH_SHORT).show()
                removeReelButton.isVisible= false
            }
            addCaption.setOnClickListener {
                if (!showingCaptionEditText) {
                    captionText.isVisible = true
                    captionText.requestFocus()
                    requireContext().showKeyboard(captionText)
                    showingCaptionEditText = false
                    addCaption.setImageResource(R.drawable.baseline_close_24)
                } else {
                    showingCaptionEditText = true
                    captionText.isVisible = false
                    hideKeyboard()
                    addCaption.setImageResource(R.drawable.baseline_text_increase_24)
                }
            }
            captionText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    captionText_ = s.toString()
                    tvCaptions.text = captionText_
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun bindObservers() {
        viewModel.reelUploadResult.observe(viewLifecycleOwner) {
            if (videoUri!=null){
                it?.onSuccess {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Reel Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                it?.onFailure {
                    Toast.makeText(requireContext(), "Some error occurred while uploading the reel", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val reelVideo = registerForActivityResult(ActivityResultContracts.GetContent()) {
        videoUri = it
        binding.removeReelButton.isVisible= it!=null
        if (it == null) {
            Toast.makeText(requireContext(), "No Video Selected", Toast.LENGTH_SHORT).show()
        } else {
            binding.removeReelButton.isVisible= true
            exoPlayer?.setMediaSource(
                ProgressiveMediaSource.Factory(
                    DefaultDataSource.Factory(requireActivity().applicationContext)
                )
                    .createMediaSource(MediaItem.fromUri(it))
            )
            exoPlayer?.playWhenReady = true
            exoPlayer?.prepare()
            exoPlayer?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.clearMediaItems()
        _binding = null
    }

}