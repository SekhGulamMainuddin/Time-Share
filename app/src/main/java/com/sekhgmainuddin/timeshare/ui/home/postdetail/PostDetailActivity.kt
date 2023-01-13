package com.sekhgmainuddin.timeshare.ui.home.postdetail

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.databinding.ActivityPostDetailBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.home.adapters.ImageVideoViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters.CommentAdapter
import com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters.LikeAdapter
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private var post: Post? = null
    private lateinit var postContentAdapter: ImageVideoViewPagerAdapter
    private lateinit var likeAdapter: LikeAdapter
    private lateinit var commentsAdapter: CommentAdapter
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("post", Post::class.java)
        } else {
            intent.getSerializableExtra("post") as Post?
        }

        post?.postId?.let { viewModel.getPost(it) }
        initialize()
        post?.let{ initializePostData(it) }
        bindObservers()

    }

    private fun bindObservers() {
        viewModel.postDetails.observe(this) {
            if (post!=it.first){
                post= it.first
                initializePostData(post!!)
            }
            if (it.second.isNotEmpty())
                binding.likeProfiles.isVisible= true
            if (it.third.isNotEmpty())
                binding.commentProfiles.isVisible= true
            likeAdapter.submitList(it.second)
            commentsAdapter.submitList(it.third)
        }

    }

    private fun initializePostData(post: Post) {
        binding.profileName.text = post.creatorName
        Glide.with(this).load(post.creatorProfileImage).placeholder(R.drawable.default_profile_pic)
            .into(binding.profileImage)
        Glide.with(this).load(post.creatorProfileImage).placeholder(R.drawable.default_profile_pic)
            .into(binding.addCommentProfileImage)
        binding.postDate.text = post.postTime.getTimeAgo()
        binding.likeCount.text = post.likeCount.toString()
        binding.commentCount.text = post.commentCount.toString()
        binding.postDescription.text = post.postDesc
        postContentAdapter = ImageVideoViewPagerAdapter(
            this, PostEntity(
                post.postId,post.creatorId,post.postDesc,
                post.postTime,post.postContent,post.creatorName,
                post.creatorProfileImage,post.likeCount,
                post.commentCount,post.likedAndCommentByMe,
                post.myComment)
        ) {
            if (it == 2) {
                binding.likeAnimation.visibility = View.VISIBLE
                binding.likeAnimation.playAnimation()
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.likeAnimation.cancelAnimation()
                    binding.likeAnimation.visibility = View.GONE
                }, 750)

            }
        }
        binding.viewPagerImageVideo.adapter= postContentAdapter
    }

    private fun initialize() {
        binding.likeProfiles.isVisible= false
        binding.commentProfiles.isVisible= false
        if (post?.myComment?.isNotEmpty() == true) {
            binding.addCommentEditText.setText(post?.myComment)
        }
        commentsAdapter= CommentAdapter(this)
        binding.commentsRecyclerView.adapter= commentsAdapter
        likeAdapter= LikeAdapter(this)
        binding.likesRecyclerView.adapter= likeAdapter
        binding.addCommentEditText.setOnKeyListener(object: OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if ((event?.action == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (binding.addCommentEditText.text.isEmpty())
                        Toast.makeText(this@PostDetailActivity, "Add some comment", Toast.LENGTH_SHORT).show()
                    else
                        post?.postId?.let { viewModel.addComment(it, binding.addCommentEditText.text.toString()) }
                    return true
                }
                return false
            }
        })
    }
}