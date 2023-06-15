package com.sekhgmainuddin.timeshare.ui.home.search.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentSearchAccountBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.search.adapters.SearchAccountAdapter
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchAccountFragment : Fragment() {

    private var _binding: FragmentSearchAccountBinding? = null
    private val binding: FragmentSearchAccountBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var accountAdapter: SearchAccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAccountBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountAdapter= SearchAccountAdapter(requireContext()){
            val bundle= Bundle()
            bundle.putSerializable("searchUser", it)
            Navigation.findNavController(requireActivity(), R.id.mainScreenFragmentContainer).navigate(R.id.profileFragment, bundle)
        }
        binding.recyclerView.adapter= accountAdapter

        bindObservers()

    }

    private fun bindObservers() {
        viewModel.profilesFromSearchQuery.observe(viewLifecycleOwner) {
            binding.progressCircular.isVisible= false
            when (it) {
                is NetworkResult.Success -> {
                    it.data?.let { profiles->
                        accountAdapter.submitList(profiles)
                    }
                    if (it.data==null)
                        Toast.makeText(requireContext(), "No Profile with the searched name found", Toast.LENGTH_SHORT).show()
                    
                }
                is NetworkResult.Error -> {
                    //Toast.makeText(requireContext(), "${it.statusCode}", Toast.LENGTH_SHORT).show()
//                    if (it.statusCode==404){
//                        Toast.makeText(requireContext(), "No Accounts Found with this prefix", Toast.LENGTH_SHORT).show()
//                    }else{
//                        Toast.makeText(requireContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show()
//                    }
                }
                is NetworkResult.Loading -> {
                    binding.progressCircular.isVisible= true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}