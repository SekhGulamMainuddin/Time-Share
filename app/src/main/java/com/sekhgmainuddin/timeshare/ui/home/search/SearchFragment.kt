package com.sekhgmainuddin.timeshare.ui.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentSearchBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment(){

    private var _binding: FragmentSearchBinding?= null
    private val binding: FragmentSearchBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var navController: NavController
    private var lastQuerySubmitted= ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentSearchBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment= childFragmentManager.findFragmentById(R.id.searchFragmentContainer) as NavHostFragment
        navController= navHostFragment.navController
        navController.navigate(R.id.searchPostFragment)

        viewModel.getAllPostsForSearchFragment(searchFragmentPost = true)

        registerClickListeners()

    }

    private fun registerClickListeners(){
        binding.apply{
            searchPostOrAccount.setOnQueryTextFocusChangeListener { v, hasFocus ->
                if (hasFocus){
                    navController.navigate(R.id.searchAccountFragment)
                    cancelAction.isVisible= true
                }
            }
            cancelAction.setOnClickListener {
                searchPostOrAccount.onActionViewCollapsed()
                navController.navigate(R.id.searchPostFragment)
            }
            searchPostOrAccount.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        lastQuerySubmitted= query
                        viewModel.getProfilesFromSearch(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}