package com.sekhgmainuddin.timeshare.ui.home.status.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentStatusBinding
import com.sekhgmainuddin.timeshare.ui.home.status.StatusViewModel
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo
import com.sekhgmainuddin.timeshare.utils.enums.StatusType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

@AndroidEntryPoint
class StatusFragment(private val statusList: List<Status>, private val user: User) : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding: FragmentStatusBinding
        get() = _binding!!
    private val viewModel by activityViewModels<StatusViewModel>()
    var currentItemIndex = 0
    private val progressList = ArrayList<ProgressBar>()
    private lateinit var status: Job

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
            val progressBar = LinearProgressIndicator(requireContext(), null)
            binding.statusProgressLayout.addView(progressBar, params)
            progressList.add(progressBar)
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
        status = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (currentItemIndex == statusList.size) {
                    withContext(Dispatchers.Main) {
                        viewModel.currentIndex.postValue(viewModel.currentIndex.value?.plus(1) ?: 0)
                    }
                    awaitCancellation()
                }
                val item = statusList[currentItemIndex]
                binding.statusUploadTime.text = item.statusUploadTime.getTimeAgo()
                val type = StatusType.valueOf(item.type)
                binding.statusText.isVisible = type == StatusType.TEXT
                binding.statusImage.isVisible = type == StatusType.IMAGE
                when (type) {
                    StatusType.IMAGE -> {
                        Glide.with(requireContext()).load(item.urlOrText).into(binding.statusImage)
                    }

                    StatusType.TEXT -> {
                        binding.statusText.text = item.urlOrText
                    }
                }
                for (i in 0 until 100) {
                    delay(50)
                    progressList[currentItemIndex].progress = i
                }
                currentItemIndex++
            }
        }
        if (currentItemIndex != statusList.size) {
            status.start()
        }
    }

    override fun onPause() {
        super.onPause()
        status.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}