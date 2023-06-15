package com.sekhgmainuddin.timeshare.ui.home.status.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentStatusBinding
import com.sekhgmainuddin.timeshare.ui.home.status.StatusViewModel
import com.sekhgmainuddin.timeshare.utils.Utils.downloadFile
import com.sekhgmainuddin.timeshare.utils.Utils.getFileDuration
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo
import com.sekhgmainuddin.timeshare.utils.enums.StatusType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class StatusFragment(private val statusList: List<Status>, private val user: User) : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding: FragmentStatusBinding
        get() = _binding!!
    private val viewModel by activityViewModels<StatusViewModel>()
    var currentItemIndex = 0
    private val progressList = ArrayList<ProgressBar>()
    private lateinit var status: Job
    private var exoPlayer: ExoPlayer? = null
    private var nextClicked= false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext()).load(user.imageUrl).into(binding.profileImage)
        binding.profileName.text = user.name

        val params = LinearLayout.LayoutParams(0, WRAP_CONTENT).apply {
            rightMargin = 10
            leftMargin = 10
            weight = 1f
        }
        statusList.forEach { _ ->
            val progressBar = LinearProgressIndicator(requireContext(), null).apply {
                setIndicatorColor(requireContext().getColor(R.color.orange))
            }
            binding.statusProgressLayout.addView(progressBar, params)
            progressList.add(progressBar)
        }

        binding.mainLayout.setOnClickListener {
            nextClicked= true
        }


    }

    override fun onResume() {
        super.onResume()
        if (currentItemIndex == statusList.size) {
            currentItemIndex = 0
            progressList.forEach {
                it.progress = 0
            }
        }
        var time: Long= 5000
        status = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                time= 5000
                if (currentItemIndex == statusList.size) {
                    withContext(Dispatchers.Main) {
                        viewModel.currentIndex.postValue(viewModel.currentIndex.value?.plus(1) ?: 0)
                    }
                    awaitCancellation()
                }
                val item = statusList[currentItemIndex]
                val type = StatusType.valueOf(item.type)
                withContext(Dispatchers.Main) {
                    binding.apply {
                        statusUploadTime.text = item.statusUploadTime.getTimeAgo()
                        statusText.isVisible = type == StatusType.TEXT
                        statusImage.isVisible = type == StatusType.IMAGE
                        statusVideo.isVisible = type == StatusType.VIDEO
                        progressCircular.isVisible =
                            (type == StatusType.IMAGE || type == StatusType.VIDEO)
                        mainLayout.setBackgroundColor(item.background)
                        captions.text= item.captions
                    }
                }
                when (type) {
                    StatusType.IMAGE -> {
                        val bitmap =
                            Glide.with(requireContext()).asBitmap().load(item.urlOrText).submit()
                                .get()
                        withContext(Dispatchers.Main) {
                            binding.apply {
                                progressCircular.isVisible = false
                                Glide.with(requireContext()).load(bitmap).into(statusImage)
                            }
                        }
                    }

                    StatusType.TEXT -> {
                        withContext(Dispatchers.Main) {
                            binding.statusText.text = item.urlOrText
                        }
                    }

                    StatusType.VIDEO -> {
                        val outputDir = requireContext().cacheDir
                        val outputFile = File.createTempFile("prefix", ".mp4", outputDir)
                        downloadFile(item.urlOrText,outputFile)
                        withContext(Dispatchers.Main){
                            time= outputFile.getFileDuration(requireContext())?:5000
                            exoPlayer= ExoPlayer.Builder(requireActivity().applicationContext).build()
                            binding.statusVideo.player= exoPlayer
                            exoPlayer?.seekTo(0)
                            exoPlayer?.setMediaSource(ProgressiveMediaSource.Factory(
                                DefaultDataSource.Factory(requireActivity().applicationContext))
                                .createMediaSource(MediaItem.fromUri(outputFile.toUri())))
                            binding.progressCircular.isVisible= false
                            exoPlayer?.playWhenReady = true
                            exoPlayer?.prepare()
                            exoPlayer?.play()
                        }
                    }
                    else->{

                    }
                }
                for (i in 0 until 100) {
                    delay(time/100)
                    if (nextClicked){
                        withContext(Dispatchers.Main){
                            progressList[currentItemIndex].progress = 100
                        }
                        break
                    }
                    withContext(Dispatchers.Main) {
                        progressList[currentItemIndex].progress = i
                    }
                }
                nextClicked= false
                currentItemIndex++
            }
        }
        if (currentItemIndex != statusList.size) {
            status.start()
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
        exoPlayer?.clearMediaItems()
        status.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}