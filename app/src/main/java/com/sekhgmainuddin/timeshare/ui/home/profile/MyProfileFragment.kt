package com.sekhgmainuddin.timeshare.ui.home.profile

import android.app.Dialog
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
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.data.modals.UserWithFriendFollowerAndFollowingLists
import com.sekhgmainuddin.timeshare.databinding.FragmentMyProfileBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.profile.adapters.ProfileViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.PostsFragment
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.ReelsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding: FragmentMyProfileBinding
        get() = _binding!!
    private lateinit var viewPagerAdapter: ProfileViewPagerAdapter
    private val fragmentNames = listOf("Posts", "Reels")
    private val viewModel by activityViewModels<HomeViewModel>()
    private var userData: User? = null
    private var userDetails: UserWithFriendFollowerAndFollowingLists? = null
    private lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog.show()

        viewModel.getUserReels()
        viewModel.getAllPosts()
        viewModel.getUserData(null)
        bindObservers()

        userData = viewModel.userData.value
        if (userData != null) {
            initialize()
        }
        loadFollowersFriendsFollowing()

    }

    private fun loadDataToViews() {
        binding.apply {
            Glide.with(requireContext()).load(userData?.imageUrl)
                .placeholder(R.drawable.default_profile_pic).into(profileImage)
            profileName.text = userData?.name
            profileBio.text = userData?.bio
            profileLocation.text = userData?.location
            verifiedIcon.isVisible = (userData?.isVerified == true)
        }
    }

    private fun initialize() {
        loadDataToViews()
        viewPagerAdapter = ProfileViewPagerAdapter(
            listOf(PostsFragment(), ReelsFragment()),
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
        }

    }

    private fun loadFollowersFriendsFollowing() {

        binding.apply {
            followerCount.text = (userData?.followers?.size?:"0").toString()
            followingCount.text =   (userData?.following?.size?:"0").toString()
            friendCount.text = (userData?.friends?.size?:"0").toString()
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
                when (user.followers.size) {
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
                        followerThreeImage.loadImage(user.followers[followersKeys[0]]?.imageUrl ?: "")
                        followerTwoImage.loadImage(user.followers[followersKeys[1]]?.imageUrl ?: "")
                        followerOneImage.loadImage(user.followers[followersKeys[2]]?.imageUrl ?: "")
                    }
                }
                when (user.following.size) {
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
                when (user.friends.size) {
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

    private fun bindObservers() {
        viewModel.userDetails.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
            it.onSuccess{_->
                it.getOrNull()?.let { user ->
                    userDetails = user
                    userData = User(
                        user.name,
                        user.userId,
                        user.email,
                        user.phone,
                        user.imageUrl,
                        user.bio?:"",
                        user.interests?: arrayListOf(),
                        user.location?:"",
                        user.activeStatus
                    )
                    loadDataToViews()
                    loadFollowersFriendsFollowing()
                }
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

