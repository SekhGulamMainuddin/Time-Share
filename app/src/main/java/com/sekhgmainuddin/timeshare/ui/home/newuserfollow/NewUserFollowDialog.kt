package com.sekhgmainuddin.timeshare.ui.home.newuserfollow


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sekhgmainuddin.timeshare.databinding.BottomSheetDialogNewUserFollowBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.newuserfollow.adapter.UserFollowAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewUserFollowDialog : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogNewUserFollowBinding? = null
    private val binding: BottomSheetDialogNewUserFollowBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var userFollowAdapter: UserFollowAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDialogNewUserFollowBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            userFollowAdapter = UserFollowAdapter(requireContext(), followClicked = {
                viewModel.followOrUnFollowFriendOrUnfriend(it, false, 0)
            }, unFollowClicked = {
                viewModel.followOrUnFollowFriendOrUnfriend(it, true, 0)
            })
            usersRecyclerView.adapter = userFollowAdapter
            progressCircular.isVisible = true
        }

        viewModel.getPopularUsers()
        bindObservers()

    }

    private fun bindObservers() {
        viewModel.popularUsers.observe(viewLifecycleOwner) {
            binding.progressCircular.isVisible = false
            it.onSuccess { userList ->
                userFollowAdapter.updateList(userList)
            }
            it.onFailure {
                Toast.makeText(
                    requireContext(),
                    "Some Error Occurred. Retry in 5 sec",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}