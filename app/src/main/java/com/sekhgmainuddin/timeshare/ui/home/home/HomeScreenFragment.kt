package com.sekhgmainuddin.timeshare.ui.home.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.databinding.FragmentHomeScreenBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ChatListActivity
import com.sekhgmainuddin.timeshare.ui.home.home.adapters.PostsAdapter
import com.sekhgmainuddin.timeshare.ui.home.home.adapters.StatusAdapter
import com.sekhgmainuddin.timeshare.ui.home.postdetail.PostDetailActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : Fragment(), PostsAdapter.OnClick {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding: FragmentHomeScreenBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private val oldFriendList= ArrayList<String>()
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentHomeScreenBinding.inflate(inflater)
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

        postsAdapter= PostsAdapter(requireContext(), this)
        val layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.postRecyclerView.layoutManager= layoutManager
        binding.postRecyclerView.adapter= postsAdapter

        viewModel.getUserData(null)
        viewModel.getUserData()


        val list= ArrayList<Status>()
        for (i in 0..10) {
            list.add(Status("",""))
        }

        Handler(Looper.getMainLooper()).postDelayed({
            postsAdapter.updateStatus(list)
            Handler(Looper.getMainLooper()).postDelayed({
                for (i in 0..10) {
                    list.add(Status("",""))
                }
                postsAdapter.updateStatus(list)
            },3000)
        },3000)

    }

    private fun bindObservers() {

        viewModel.userData.observe(viewLifecycleOwner) {
            it.friends?.let { friends ->
                if (friends!=oldFriendList) {
                    viewModel.getLatestPosts(friends)
                    oldFriendList.clear()
                    oldFriendList.addAll(friends)
                }
            }
        }

        viewModel.allPosts.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                postsAdapter.submitList(it)
        }
        viewModel.userDetails.observe(viewLifecycleOwner){
            it.onSuccess {
                progressDialog.dismiss()
            }
            it.onFailure {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun registerListeners(){
        binding.messages.setOnClickListener {
            startActivity(Intent(requireContext(), ChatListActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

    override fun postClicked(post: PostEntity) {
        val postData= Post(post.postId, post.creatorId, post.postDesc, post.postContent,
                        post.creatorName, post.creatorProfileImage, post.postTime,
                        post.likesCount, post.commentCount, post.likedAndCommentByMe, post.myComment)
        startActivity(Intent(requireContext(), PostDetailActivity::class.java).putExtra("post",postData))
    }

    override fun savePost(post: PostEntity) {

    }

    override fun sharePost(post: PostEntity) {

    }

    override fun likePost(postId: String) {
        viewModel.addLike(postId)
    }

}