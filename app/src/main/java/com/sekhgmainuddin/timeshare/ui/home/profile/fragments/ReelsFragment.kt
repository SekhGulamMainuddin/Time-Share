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
import com.sekhgmainuddin.timeshare.utils.Constants
import com.sekhgmainuddin.timeshare.utils.Constants.TYPE_USER_LOGGED_IN
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReelsFragment(val type: Int= TYPE_USER_LOGGED_IN): Fragment(){

    private var _binding: FragmentReelsBinding?= null
    private val binding: FragmentReelsBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private val previousReels= ArrayList<String>()
    private lateinit var adapter: ProfileReelsAdapter
    private lateinit var selectedReelFragment: SelectedReelFragment

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

        initialize()
        bindObservers()

    }

    private fun initialize() {
        selectedReelFragment= SelectedReelFragment()
        adapter= ProfileReelsAdapter(requireContext()){
            viewModel.reelPassedToView.postValue(it)
            selectedReelFragment.show(childFragmentManager, "selectedReel")
        }
        binding.profileReelsRecyclerView.adapter= adapter
    }

    private fun bindObservers(){

        if (type==TYPE_USER_LOGGED_IN){
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
        }else{

            viewModel.searchUserUploadedReels.observe(viewLifecycleOwner){
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}