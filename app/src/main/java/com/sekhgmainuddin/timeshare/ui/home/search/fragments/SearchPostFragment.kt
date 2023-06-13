package com.sekhgmainuddin.timeshare.ui.home.search.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.databinding.FragmentSearchPostBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.postdetail.PostDetailFragment
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.SelectedPostFragment
import com.sekhgmainuddin.timeshare.ui.home.search.adapters.SearchPostAdapter
import com.sekhgmainuddin.timeshare.ui.home.search.layoutmanager.SpanSize
import com.sekhgmainuddin.timeshare.ui.home.search.layoutmanager.SpannedGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchPostFragment: Fragment(){

    private var _binding: FragmentSearchPostBinding?= null
    private val binding: FragmentSearchPostBinding
        get() = _binding!!
    private lateinit var postsAdapter: SearchPostAdapter
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var selectedPostFragment: SelectedPostFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentSearchPostBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        bindObservers()

    }

    private fun initialize() {
        selectedPostFragment= SelectedPostFragment()
        val spannedGridLayoutManager = SpannedGridLayoutManager(
            orientation = SpannedGridLayoutManager.Orientation.VERTICAL,
            spans = 3
        )
        spannedGridLayoutManager.itemOrderIsStable = true
        spannedGridLayoutManager.spanSizeLookup =
            SpannedGridLayoutManager.SpanSizeLookup { position ->

                var x = 0
                if (position % 9 == 0) {
                    x = position / 9
                }
                when {
                    position == 1 || x % 2 == 1 || (position - 1) % 18 == 0 ->
                        SpanSize(2, 2)
                    else ->
                        SpanSize(1, 1)
                }

            }
        postsAdapter= SearchPostAdapter(requireContext(),{
            startActivity(Intent(requireContext(), PostDetailFragment::class.java).putExtra("post",it))
        },{
            viewModel.postPassedToView.postValue(it)
            selectedPostFragment.show(childFragmentManager, "selectedPostFragment")
        })
        binding.apply {
            progressCircular.isVisible= true
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager= spannedGridLayoutManager
                adapter= postsAdapter
            }
        }
    }

    private fun bindObservers(){
        val list= ArrayList<Pair<Post, String>>()
        viewModel.searchFragmentPosts.observe(viewLifecycleOwner){
            if (!it.isNullOrEmpty()){
                postsAdapter.submitList(it)
                binding.progressCircular.isVisible= false
            }
            Log.d("postsReceived", "bindObservers: $it")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}