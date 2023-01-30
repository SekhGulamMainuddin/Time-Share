package com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments

import android.content.Intent
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
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.AddNewPostOrReelActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNewPostReelStatusBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddPostDialogBinding?= null
    private val binding: BottomSheetAddPostDialogBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= BottomSheetAddPostDialogBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()

    }

    private fun initialize() {
        binding.apply {
            Glide.with(requireContext()).load(viewModel.userData.value?.imageUrl).placeholder(R.drawable.default_profile_pic).into(profileImage)
            addStatusButton.setOnClickListener {
                startActivity(Intent(requireContext(), AddNewPostOrReelActivity::class.java).putExtra("type",0))
                dismiss()
            }
            addPostButton.setOnClickListener {
                startActivity(Intent(requireContext(), AddNewPostOrReelActivity::class.java).putExtra("type",1))
                dismiss()
            }
            addReelsButton.setOnClickListener {
                startActivity(Intent(requireContext(), AddNewPostOrReelActivity::class.java).putExtra("type",2))
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}