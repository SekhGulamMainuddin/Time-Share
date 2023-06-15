package com.sekhgmainuddin.timeshare.ui.home.profile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentPostsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.profile.adapters.ProfilePostsAdapter
import com.sekhgmainuddin.timeshare.utils.Constants.TYPE_USER_LOGGED_IN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostsFragment(val type: Int= TYPE_USER_LOGGED_IN) : Fragment(){

    private var _binding: FragmentPostsBinding?= null
    private val binding: FragmentPostsBinding
        get() = _binding!!
    private var widthForPostView: Int= 0
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var postsAdapter: ProfilePostsAdapter
    private lateinit var selectedPostFragment: SelectedPostFragment

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
        selectedPostFragment= SelectedPostFragment()
        postsAdapter= ProfilePostsAdapter(requireContext()) { it, instantView->
            val bundle= Bundle()
            bundle.putSerializable("post",it)
            if (instantView){
                viewModel.postPassedToView.postValue(it)
                selectedPostFragment.show(childFragmentManager,"")
            }else{
                if (type== TYPE_USER_LOGGED_IN){
                    findNavController().navigate(R.id.action_myProfileFragment_to_postDetailFragment, args = bundle)
                }else{
                    findNavController().navigate(R.id.action_profileFragment_to_postDetailFragment, args = bundle)
                }
            }
        }
        binding.postsRecyclerView.adapter = postsAdapter
    }

    private fun bindObservers(){
        if (type== TYPE_USER_LOGGED_IN){
            viewModel.posts.observe(viewLifecycleOwner){
                it?.let {
                    postsAdapter.submitList(it)
                }
            }
        }else{
            viewModel.otherUserPosts.observe(viewLifecycleOwner){
                it?.let {
                    postsAdapter.submitList(it)
                }
            }
        }
    }

}