package com.sekhgmainuddin.timeshare.ui.home.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.ArraySet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentHomeScreenBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.home.adapters.PostsAdapter
import com.sekhgmainuddin.timeshare.ui.home.notification.NotificationsActivity
import com.sekhgmainuddin.timeshare.ui.home.status.StatusActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class HomeScreenFragment : Fragment(), PostsAdapter.OnClick {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding: FragmentHomeScreenBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private val oldFriendList = ArrayList<String>()
    private val oldFollowingList = ArrayList<String>()
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var progressDialog: Dialog
    private var myStatus: Pair<List<Status>, User>? = null
    private val statusList = ArrayList<Pair<List<Status>, User>>()
    private val listProfileIds = ArraySet<String>()
    private val postList = ArrayList<PostEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeScreenBinding.inflate(inflater)
        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        registerListeners()
        bindObservers()

    }

    private fun initialize() {
        progressDialog.show()

        postsAdapter = PostsAdapter(requireContext(), this)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.apply {
            postRecyclerView.layoutManager = layoutManager
            postRecyclerView.adapter = postsAdapter
            postsAdapter.updateStatus(
                listOf(
                    Pair(
                        listOf(Status()),
                        User()
                    )
                )
            )
        }

        viewModel.getUserData(null)
        viewModel.getUserData()
    }

    private fun bindObservers() {
        viewModel.userData.observe(viewLifecycleOwner) {
            if (it.friends != oldFriendList || it.following != oldFollowingList) {
                listProfileIds.clear()
                listProfileIds.addAll(it.friends + it.following + listOf(it.userId))
                oldFriendList.clear()
                oldFriendList.addAll(it.friends)
                oldFollowingList.clear()
                oldFollowingList.addAll(it.friends)
                viewModel.otherUsersId.clear()
                viewModel.otherUsersId.addAll(it.friends)
                viewModel.otherUsersId.addAll(it.following)
                viewModel.otherUsersId.add(it.userId)
                viewModel.getLatestPosts(listProfileIds.toList())
                postsAdapter.updateFollowingAndFriendIds(viewModel.otherUsersId)
                postsAdapter.userImageUrl= it.imageUrl
            }
        }
        viewModel.allPosts.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.mainLayout.isRefreshing = false
                postList.clear()
                postList.add(PostEntity())
                postList.addAll(it)
                postsAdapter.updateFollowingAndFriendIds(viewModel.otherUsersId)
                postsAdapter.updatePostList(postList)
            }
        }
        viewModel.userDetails.observe(viewLifecycleOwner) {
            it.onSuccess {
                progressDialog.dismiss()
            }
            it.onFailure {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to fetch user details", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        viewModel.statusListWithUserDetail.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()){
                statusList.clear()
                statusList.addAll(it)
                postsAdapter.updateStatus(statusList)
            }
        }
        viewModel.likeResult.observe(viewLifecycleOwner) {
            it.onSuccess {  post->
                postsAdapter.updateItem(post)
            }
            it.onFailure {
                Toast.makeText(requireContext(), "Failed to like the Post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun registerListeners() {
        binding.apply {
            messages.setOnClickListener {
                findNavController().navigate(R.id.action_homeScreenFragment_to_chatsListFragment2)
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).isVisible= false
            }
            mainLayout.setOnRefreshListener {
                mainLayout.isRefreshing = true
                viewModel.getLatestPosts(listProfileIds.toList())
            }
            notifications.setOnClickListener {
                startActivity(Intent(requireContext(), NotificationsActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun postClicked(post: PostEntity) {
        val postData = Post(
            post.postId, post.creatorId, post.postDesc, post.postContent,
            post.creatorName, post.creatorProfileImage, post.postTime,
            post.likesCount, post.commentCount, post.likedAndCommentByMe, post.myComment
        )
        val bundle = Bundle()
        bundle.putSerializable("post", postData)
        findNavController().navigate(
            R.id.action_homeScreenFragment_to_postDetailFragment,
            args = bundle
        )
    }

    override fun savePost(post: PostEntity) {

    }

    override fun sharePost(post: PostEntity) {

    }

    override fun likePost(post: PostEntity) {
        viewModel.addLike(post)
    }

    override fun followId(profileId: String) {
        viewModel.followOrUnFollowFriendOrUnfriend(profileId, false, 0)
    }

    override fun showStatus(position: Int) {
        if (position == 0 && myStatus?.first?.get(0)?.statusUploadTime == -1L) {
            findNavController().navigate(R.id.action_homeScreenFragment_to_addStatusFragment)
        } else {
            startActivity(
                Intent(requireContext(), StatusActivity::class.java)
                    .putExtra("startIndex", position)
                    .putExtra("statusData", Json.encodeToString(statusList))
            )
        }
    }

}