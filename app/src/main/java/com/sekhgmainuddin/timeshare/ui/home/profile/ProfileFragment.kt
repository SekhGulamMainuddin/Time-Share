package com.sekhgmainuddin.timeshare.ui.home.profile

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.data.modals.UserWithFriendFollowerAndFollowingLists
import com.sekhgmainuddin.timeshare.databinding.FragmentProfileBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.profile.adapters.ProfileViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.PostsFragment
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.ReelsFragment
import com.sekhgmainuddin.timeshare.utils.Constants.TYPE_SEARCH_USER
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding!!
    private lateinit var viewPagerAdapter: ProfileViewPagerAdapter
    private val fragmentNames = listOf("Posts", "Reels")
    private val viewModel by activityViewModels<HomeViewModel>()
    private val chatsViewModel by activityViewModels<ChatsViewModel>()
    private var userData: User? = null
    private var userDetails: UserWithFriendFollowerAndFollowingLists? = null
    private lateinit var progressDialog: Dialog
    private var userId: String? = null
    private var isCalled= false

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var uid = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater)
        userData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("searchUser", User::class.java)
        } else {
            arguments?.getSerializable("searchUser") as User
        }
        userId = userData?.userId
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog.show()

        uid = firebaseAuth.currentUser?.uid ?: ""

        userId?.let {
            viewModel.getUserReels(userId = it)
            viewModel.getAllPosts(userId = it)
            viewModel.getUserData(it)
            bindObservers()

            if (userData != null) {
                initialize()
            }
            loadFollowersFriendsFollowing()
        }
    }

    private fun loadDataToViews() {
        binding.apply {
            Glide.with(requireContext()).load(userData?.imageUrl)
                .placeholder(R.drawable.default_profile_pic).into(profileImage)
            profileName.text = userData?.name
            profileBio.text = userData?.bio
            profileLocation.text = userData?.location
            verifiedIcon.isVisible = (userData?.isVerified == true)
            unFollowOrFollowButton.text= getString(if (chatsViewModel.followingList.contains(userId)) R.string.un_follow else R.string.follow)
            unFriendOrRequestButton.text = getString(if (chatsViewModel.friendsList.contains(userId)) R.string.un_friend else R.string.add_friend)
        }
    }

    private fun initialize() {
        loadDataToViews()
        viewPagerAdapter = ProfileViewPagerAdapter(
            listOf(PostsFragment(TYPE_SEARCH_USER), ReelsFragment(TYPE_SEARCH_USER)),
            childFragmentManager,
            lifecycle
        )

        binding.apply {
            viewPager2.adapter = viewPagerAdapter
            TabLayoutMediator(
                profileTabs,
                viewPager2
            ) { tab, position ->
                tab.text = fragmentNames[position]
            }.attach()
            unFriendOrRequestButton.text =
                getString(if (chatsViewModel.friendsList.contains(userId)) R.string.un_friend else R.string.add_friend)
            unFollowOrFollowButton.text =
                getString(if (chatsViewModel.followingList.contains(userId)) R.string.un_follow else R.string.follow)

            unFollowOrFollowButton.setOnClickListener {
                viewModel.followOrUnFollowFriendOrUnfriend(
                    userId!!,
                    chatsViewModel.followingList.contains(userId),
                    0
                )
                unFollowOrFollowButton.text= getString(if (chatsViewModel.followingList.contains(userId)) R.string.follow else R.string.un_follow)
                isCalled= true
            }
            unFriendOrRequestButton.setOnClickListener {
                if (chatsViewModel.friendsList.contains(userId)) {
                    viewModel.followOrUnFollowFriendOrUnfriend(userId!!, true, 1)
                    unFriendOrRequestButton.text = getString(R.string.add_friend)
                }else{
                    viewModel.addFriendRequest(userData!!)
                    unFriendOrRequestButton.text = getString(R.string.un_friend)
                }
                isCalled= true
            }

        }

    }

    private fun loadFollowersFriendsFollowing() {

        binding.apply {
            followerCount.text = (userData?.followers?.size ?: "0").toString()
            followingCount.text = (userData?.following?.size ?: "0").toString()
            friendCount.text = (userData?.friends?.size ?: "0").toString()
            followerOneImage.isVisible = false
            followerTwoImage.isVisible = false
            followerThreeImage.isVisible = false
            followingThreeImage.isVisible = false
            followingTwoImage.isVisible = false
            followingOneImage.isVisible = false
            friendsThreeImage.isVisible = false
            friendsTwoImage.isVisible = false
            friendsOneImage.isVisible = false
        }
        userDetails?.let { user ->
            binding.apply {
                followingCount.text = user.following.size.toString()
                followerCount.text = user.followers.size.toString()
                friendCount.text = user.friends.size.toString()
                val followersKeys = user.followers.keys.toList()
                val followingKeys = user.following.keys.toList()
                val friendsKeys = user.friends.keys.toList()
                if (followersKeys.isNotEmpty()){
                    when (user.followers.size) {
                        0->{}
                        1 -> {
                            followerOneImage.isVisible = true
                            followerOneImage.loadImage(
                                user.followers[followersKeys[0]]?.imageUrl ?: ""
                            )
                        }

                        2 -> {
                            followerOneImage.isVisible = true
                            followerTwoImage.isVisible = true
                            followerOneImage.loadImage(
                                user.followers[followersKeys[0]]?.imageUrl ?: ""
                            )
                            followerTwoImage.loadImage(user.followers[followersKeys[1]]?.imageUrl ?: "")
                        }

                        else -> {
                            followerThreeImage.isVisible = true
                            followerTwoImage.isVisible = true
                            followerOneImage.isVisible = true
                            followerThreeImage.loadImage(
                                user.followers[followersKeys[0]]?.imageUrl ?: ""
                            )
                            followerTwoImage.loadImage(user.followers[followersKeys[1]]?.imageUrl ?: "")
                            followerOneImage.loadImage(user.followers[followersKeys[2]]?.imageUrl ?: "")
                        }
                    }
                }
                if (followingKeys.isNotEmpty()){
                    when (user.following.size) {
                        0->{}
                        1 -> {
                            followingOneImage.isVisible = true
                            followingOneImage.loadImage(
                                user.following[followingKeys[0]]?.imageUrl ?: ""
                            )
                        }

                        2 -> {
                            followingOneImage.isVisible = true
                            followingTwoImage.isVisible = true
                            followingOneImage.loadImage(
                                user.following[followingKeys[0]]?.imageUrl ?: ""
                            )
                            followingTwoImage.loadImage(
                                user.following[followingKeys[1]]?.imageUrl ?: ""
                            )
                        }

                        else -> {
                            followingThreeImage.isVisible = true
                            followingTwoImage.isVisible = true
                            followingOneImage.isVisible = true
                            followingThreeImage.loadImage(
                                user.following[followingKeys[0]]?.imageUrl ?: ""
                            )
                            followingTwoImage.loadImage(
                                user.following[followingKeys[1]]?.imageUrl ?: ""
                            )
                            followingOneImage.loadImage(
                                user.following[followingKeys[2]]?.imageUrl ?: ""
                            )
                        }
                    }
                }
                if (followingKeys.isNotEmpty()){
                    when (user.friends.size) {
                        0->{}
                        1 -> {
                            friendsOneImage.isVisible = true
                            friendsOneImage.loadImage(user.friends[friendsKeys[0]]?.imageUrl ?: "")
                        }

                        2 -> {
                            friendsOneImage.isVisible = true
                            friendsTwoImage.isVisible = true
                            friendsOneImage.loadImage(user.friends[friendsKeys[0]]?.imageUrl ?: "")
                            friendsTwoImage.loadImage(user.friends[friendsKeys[1]]?.imageUrl ?: "")
                        }
                        else -> {
                            friendsThreeImage.isVisible = true
                            friendsTwoImage.isVisible = true
                            friendsOneImage.isVisible = true
                            friendsThreeImage.loadImage(user.friends[friendsKeys[0]]?.imageUrl ?: "")
                            friendsTwoImage.loadImage(user.friends[friendsKeys[1]]?.imageUrl ?: "")
                            friendsOneImage.loadImage(user.friends[friendsKeys[2]]?.imageUrl ?: "")
                        }
                    }
                }
            }
        }
    }

    private fun bindObservers() {
        viewModel.searchUserDetails.observe(viewLifecycleOwner) {
            if (it.getOrNull()?.userId==userId){
                progressDialog.dismiss()
                it.onSuccess { _ ->
                    it.getOrNull()?.let { user ->
                        userDetails = user
                        userData = User(
                            user.name,
                            user.userId,
                            user.email,
                            user.phone,
                            user.imageUrl,
                            user.bio ?: "",
                            user.interests ?: arrayListOf(),
                            user.location ?: "",
                            user.activeStatus
                        )
                        loadDataToViews()
                        loadFollowersFriendsFollowing()
                    }
                }
                it.onFailure { t ->
                    Toast.makeText(requireContext(), "$t", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.followFriendResult.observe(viewLifecycleOwner) {
            if (isCalled){
                it.onSuccess {
                    Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT)
                        .show()
                }
                it.onFailure {
                    Toast.makeText(
                        requireContext(),
                        "Failed to Update Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isCalled= false
            }
        }
    }

    private fun ImageView.loadImage(url: String) {
        Glide.with(requireContext()).load(url).placeholder(R.drawable.default_profile_pic)
            .into(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

