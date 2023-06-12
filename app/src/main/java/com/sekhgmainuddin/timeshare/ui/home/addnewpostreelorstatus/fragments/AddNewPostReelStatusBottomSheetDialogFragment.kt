package com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.BottomSheetAddPostDialogBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNewPostReelStatusBottomSheetDialogFragment(val onClick: (Int) -> Unit) :
    BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddPostDialogBinding? = null
    private val binding: BottomSheetAddPostDialogBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddPostDialogBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()

    }

    private fun initialize() {
        binding.apply {
            Glide.with(requireContext()).load(viewModel.userData.value?.imageUrl)
                .placeholder(R.drawable.default_profile_pic).into(profileImage)
            addStatusButton.setOnClickListener {
                onClick.invoke(0)
                dismiss()
            }
            addPostButton.setOnClickListener {
                onClick.invoke(1)
                dismiss()
            }
            addReelsButton.setOnClickListener {
                onClick.invoke(2)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}