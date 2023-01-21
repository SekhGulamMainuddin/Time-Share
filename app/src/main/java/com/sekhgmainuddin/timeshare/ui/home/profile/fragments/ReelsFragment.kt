package com.sekhgmainuddin.timeshare.ui.home.profile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sekhgmainuddin.timeshare.databinding.FragmentReelsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.profile.adapters.ProfileReelsAdapter
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReelsFragment: Fragment(){

    private var _binding: FragmentReelsBinding?= null
    private val binding: FragmentReelsBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private val previousReels= ArrayList<String>()
    private lateinit var adapter: ProfileReelsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentReelsBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUserReels()
        initialize()
        bindObservers()

    }

    private fun initialize() {
        adapter= ProfileReelsAdapter(requireContext())
        binding.profileReelsRecyclerView.adapter= adapter
    }

    private fun bindObservers(){
        viewModel.userUploadedReels.observe(viewLifecycleOwner){
            when(it){
                is NetworkResult.Success-> {
                    it.data?.let { newReels ->
                        newReels.forEach {  reel ->
                            previousReels.add(reel.reelId)
                        }
                        adapter.submitList(newReels)
                    }
                }
                is NetworkResult.Error-> {

                }
                is NetworkResult.Loading-> {

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}