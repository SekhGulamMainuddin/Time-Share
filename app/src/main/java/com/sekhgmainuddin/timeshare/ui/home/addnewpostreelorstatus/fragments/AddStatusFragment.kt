package com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentAddStatusBinding
import com.sekhgmainuddin.timeshare.ui.customviews.ColorSeekBar
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.utils.Utils.getFileExtension
import com.sekhgmainuddin.timeshare.utils.Utils.hideKeyboard
import com.sekhgmainuddin.timeshare.utils.Utils.isImageOrVideo
import com.sekhgmainuddin.timeshare.utils.Utils.showKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddStatusFragment : Fragment() {

    private var _binding: FragmentAddStatusBinding? = null
    private val binding: FragmentAddStatusBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private var statusText_ = ""
    private var captionsText = ""
    private var imageOrVideoUri: Uri? = null
    private var backgroundColor = Color.BLACK
    private var exoPlayer: ExoPlayer? = null
    private var type = -1
    private lateinit var progressDialog: Dialog
    private var initial = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStatusBinding.inflate(inflater)
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
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/*", "video/*")
        )
        intent.action = Intent.ACTION_GET_CONTENT
        binding.apply {

            exoPlayer = ExoPlayer.Builder(requireActivity().applicationContext).build()
            statusVideo.player = exoPlayer
            exoPlayer?.seekTo(0)
            exoPlayer?.repeatMode = REPEAT_MODE_ALL

            Glide.with(requireContext()).load(viewModel.userData.value?.imageUrl).into(profileImage)
            profileName.text = viewModel.userData.value?.name
            addContent.shrink()
            addContent.setOnClickListener {
                if (addBackground.isVisible) {
                    addContent.shrink()
                    addBackground.isVisible = false
                    addImageOrVideo.isVisible = false
                    addText.isVisible = false
                } else {
                    addContent.extend()
                    addBackground.isVisible = true
                    addImageOrVideo.isVisible = true
                    addText.isVisible = true
                }
            }
            captionsInputET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    captionsText = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            statusText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    statusText_ = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            addBackground.setOnClickListener {
                hideKeyboard()
                colorSeekBar.isVisible = !colorSeekBar.isVisible
                addBackground.setImageResource(if (colorSeekBar.isVisible) R.drawable.baseline_close_24 else R.drawable.baseline_window_24)
            }
            addText.setOnClickListener {
                if (imageOrVideoUri != null) {
                    Toast.makeText(
                        requireContext(),
                        "Please add text in the caption part",
                        Toast.LENGTH_SHORT
                    ).show()
                    captionsInputET.requestFocus()
                    requireContext().showKeyboard(captionsInputET)
                } else {
                    statusText.isVisible = true
                    statusText.requestFocus()
                    requireContext().showKeyboard(statusText)
                    type = -1
                }
            }
            addImageOrVideo.setOnClickListener {
                progressCircular.isVisible = true
                hideKeyboard()
                statusText.setText("")
                statusText.isVisible = false
                imageOrVideo.launch(intent)
            }
            colorSeekBar.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
                override fun onColorChangeListener(color: Int) {
                    backgroundColor = color
                    binding.mainLayout.setBackgroundColor(backgroundColor)
                }
            })
            removeImageOrVideo.setOnClickListener {
                imageOrVideoUri = null
                statusImage.setImageDrawable(null)
                exoPlayer?.pause()
                exoPlayer?.clearMediaItems()
                statusImage.isVisible = false
                statusVideo.isVisible = false
                captionsInput.isVisible = false
                removeImageOrVideo.isVisible = false
                Toast.makeText(requireContext(), "Removed Item", Toast.LENGTH_SHORT).show()
                captionsInputET.setText("")
                type = -1
            }
            uploadButton.setOnClickListener {
                initial= 1
                hideKeyboard()
                if (statusText_.isEmpty() && imageOrVideoUri == null && type == -1 && captionsText.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Add Some Content first...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    progressDialog.show()
                    viewModel.uploadStatus(
                        statusText_,
                        imageOrVideoUri,
                        type,
                        captionsText,
                        backgroundColor
                    )
                }
            }
        }
    }

    private fun bindObservers() {
        viewModel.statusUploadResult.observe(viewLifecycleOwner) {
            if (initial==1){
                progressDialog.dismiss()
                it.onSuccess {
                    Toast.makeText(requireContext(), "Status Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                it.onFailure {
                    Toast.makeText(requireContext(), "Some error occurred while uploading the status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val imageOrVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { d ->
            val it = d.data?.data
            imageOrVideoUri = it
            binding.statusImage.isVisible = it != null
            binding.removeImageOrVideo.isVisible = it != null
            binding.progressCircular.isVisible = false
            it?.let { i ->
                when (getFileExtension(i, requireContext())?.isImageOrVideo()) {
                    0 -> {
                        Glide.with(requireContext()).load(i).into(binding.statusImage)
                        binding.captionsInput.isVisible = true
                        type = 0
                    }

                    1 -> {
                        binding.captionsInput.isVisible = true
                        binding.statusVideo.isVisible = true
                        exoPlayer?.setMediaSource(
                            ProgressiveMediaSource.Factory(
                                DefaultDataSource.Factory(requireActivity().applicationContext)
                            )
                                .createMediaSource(MediaItem.fromUri(i))
                        )
                        exoPlayer?.playWhenReady = true
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                        type = 1
                    }

                    else -> {
                        Toast.makeText(requireContext(), "File not supported", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
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