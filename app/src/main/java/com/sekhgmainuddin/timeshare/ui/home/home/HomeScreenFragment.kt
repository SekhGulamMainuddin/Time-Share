package com.sekhgmainuddin.timeshare.ui.home.home

import android.content.Intent
import android.os.Bundle
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
    private lateinit var statusAdapter: StatusAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentHomeScreenBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        registerListeners()
        bindObservers()

    }

    private fun initialize() {
        postsAdapter= PostsAdapter(requireContext(), this)
        val layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.postRecyclerView.layoutManager= layoutManager
        binding.postRecyclerView.adapter= postsAdapter

        viewModel.getUserData()

        statusAdapter= StatusAdapter(requireContext())
        binding.statusRecyclerView.adapter= statusAdapter

        val list= ArrayList<Status>()
        for (i in 0..10) {
            list.add(Status("",""))
        }
        statusAdapter.submitList(list)

        binding.postRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                binding.statusRecyclerView.isVisible = layoutManager.findFirstVisibleItemPosition()==0

                super.onScrolled(recyclerView, dx, dy)

            }
        })

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
            Toast.makeText(requireContext(), "${it.size}", Toast.LENGTH_LONG).show()
            for (i in it) {
                Log.d("latestPosts", "bindObservers: ${i.postTime} ${i.creatorName}")
            }
            postsAdapter.submitList(it)
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
                        post.creatorName, post.creatorProfileImage, post.likesCount,
                        post.commentCount, post.likedAndCommentByMe)
        startActivity(Intent(requireContext(), PostDetailActivity::class.java).putExtra("post",postData))
    }

}