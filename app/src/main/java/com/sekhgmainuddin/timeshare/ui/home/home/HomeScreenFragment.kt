package com.sekhgmainuddin.timeshare.ui.home.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentHomeScreenBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.chatlist.ChatListActivity
import com.sekhgmainuddin.timeshare.ui.home.home.adapters.PostsAdapter
import com.sekhgmainuddin.timeshare.ui.home.postdetail.PostDetailActivity
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
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var progressDialog: Dialog
    private var myStatus: Pair<List<Status>, User>? = null
    private val statusList = ArrayList<Pair<List<Status>, User>>()

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
            if (it.friends != oldFriendList) {
                val list = it.friends + it.following + listOf(it.userId)
                viewModel.getLatestPosts(list.toSet().toMutableList())
                oldFriendList.clear()
                oldFriendList.addAll(it.friends)
            }
        }
        viewModel.allPosts.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                postsAdapter.submitList(it)
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
            statusList.clear()
            statusList.addAll(it)
            Log.d("statusList", "bindObservers: ${it.size}")
            postsAdapter.updateStatus(statusList)
        }
    }

    fun registerListeners() {
        binding.messages.setOnClickListener {
            startActivity(Intent(requireContext(), ChatListActivity::class.java))
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
        startActivity(
            Intent(requireContext(), PostDetailActivity::class.java).putExtra(
                "post",
                postData
            )
        )
    }

    override fun savePost(post: PostEntity) {

    }

    override fun sharePost(post: PostEntity) {

    }

    override fun likePost(postId: String) {
        viewModel.addLike(postId)
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