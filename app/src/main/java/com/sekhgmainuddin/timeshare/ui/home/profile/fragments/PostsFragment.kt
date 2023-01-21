package com.sekhgmainuddin.timeshare.ui.home.profile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sekhgmainuddin.timeshare.databinding.FragmentPostsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.profile.adapters.ProfilePostsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostsFragment : Fragment(){

    private var _binding: FragmentPostsBinding?= null
    private val binding: FragmentPostsBinding
        get() = _binding!!
    private var widthForPostView: Int= 0
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var postsAdapter: ProfilePostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentPostsBinding.inflate(inflater)

        widthForPostView= (resources.displayMetrics.widthPixels/(resources.displayMetrics.density*3)).toInt()

        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        bindObservers()

    }

    private fun initialize(){
        postsAdapter= ProfilePostsAdapter(requireContext())
        binding.postsRecyclerView.adapter = postsAdapter
    }

    private fun bindObservers(){
        viewModel.posts.observe(viewLifecycleOwner){
            it?.let {
                postsAdapter.submitList(it)
            }
        }
    }

}