package com.sekhgmainuddin.timeshare.ui.home.postdetail

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.ExoPlayerItem
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.databinding.FragmentPostDetailBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.adapters.ImageVideoViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.adapters.onClick
import com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters.CommentAdapter
import com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters.LikeAdapter
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment : Fragment(), onClick {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding: FragmentPostDetailBinding
        get() = _binding!!
    private var post: Post? = null
    private lateinit var postContentAdapter: ImageVideoViewPagerAdapter
    private lateinit var likeAdapter: LikeAdapter
    private lateinit var commentsAdapter: CommentAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private val viewModel by activityViewModels<HomeViewModel>()
    private var isAudioMuted = false
    private var lastPlayIndex: Int?= null
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var uid = ""
    private lateinit var progressDialog: Dialog
    private var requestFromHere = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog.show()

        uid = firebaseAuth.currentUser?.uid ?: ""

        post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("post", Post::class.java)
        } else {
            arguments?.getSerializable("post") as Post?
        }

        post?.postId?.let { viewModel.getPost(it) }
        initialize()
        post?.let { initializePostData(it) }
        bindObservers()

    }

    private fun initialize() {
        binding.apply {
            likeProfiles.isVisible = false
            commentProfiles.isVisible = false
            if (post?.myComment?.isNotEmpty() == true) {
                binding.addCommentEditText.setText(post?.myComment)
            }
            commentsAdapter = CommentAdapter(requireContext())
            commentsRecyclerView.adapter = commentsAdapter
            likeAdapter = LikeAdapter(requireContext())
            likesRecyclerView.adapter = likeAdapter
            addCommentEditText.setOnKeyListener(object : OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if ((event?.action == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)
                    ) {
                        if (addCommentEditText.text.isEmpty())
                            Toast.makeText(
                                requireContext(),
                                "Add some comment",
                                Toast.LENGTH_SHORT
                            ).show()
                        else
                            post?.let {
                                viewModel.addComment(
                                    it,
                                    addCommentEditText.text.toString()
                                )
                            }
                        return true
                    }
                    return false
                }
            })
            likeButton.setOnClickListener {
                post?.apply {
                    viewModel.addLike(
                        PostEntity(
                            postId,
                            creatorId,
                            postDesc,
                            postTime,
                            postContent,
                            creatorName,
                            creatorProfileImage,
                            likeCount,
                            commentCount,
                            likedAndCommentByMe,
                            myComment
                        )
                    )
                }
                showLikeAnimation()
            }
            profileName.setOnClickListener {
                progressDialog.show()
                viewModel.getUserData(post?.creatorId)
                requestFromHere= true
            }
            followTV.setOnClickListener {
                post?.creatorId?.let {
                        it1 -> viewModel.followOrUnFollowFriendOrUnfriend(it1, false, 0)
                }
                Toast.makeText(requireContext(), "$post", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initializePostData(post: Post) {
        binding.apply {
            this@PostDetailFragment.post= post
            profileName.text = post.creatorName
            Glide.with(this@PostDetailFragment).load(post.creatorProfileImage)
                .placeholder(R.drawable.default_profile_pic)
                .into(profileImage)
            Glide.with(this@PostDetailFragment).load(viewModel.userData.value?.imageUrl)
                .placeholder(R.drawable.default_profile_pic)
                .into(addCommentProfileImage)
            postDate.text = post.postTime.getTimeAgo()
            likeCount.text = post.likeCount.toString()
            commentCount.text = post.commentCount.toString()
            postDescription.text = post.postDesc
            likeButton.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    if (post.likedAndCommentByMe in intArrayOf(
                            1,
                            3
                        )
                    ) R.drawable.liked_icon else R.drawable.love_icon
                )
            )
            commentButton.imageTintList = ColorStateList.valueOf(
                requireContext().getColor(
                    if (post.likedAndCommentByMe in intArrayOf(
                            2,
                            3
                        )
                    ) R.color.orangePink else R.color.black
                )
            )
            postContentAdapter = ImageVideoViewPagerAdapter(
                requireContext(),
                this@PostDetailFragment,
                object : ImageVideoViewPagerAdapter.OnVideoListener {
                    override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                        exoPlayerItems.add(exoPlayerItem)
                    }

                    override fun onVideoClick(position: Int) {
                        isAudioMuted = !isAudioMuted

                        val index =
                            exoPlayerItems.indexOfFirst { it.position == binding.viewPagerImageVideo.currentItem }
                        if (index != -1) {
                            val player = exoPlayerItems[index].exoPlayer
                            player.volume = if (isAudioMuted) 0f else 1f
                        }

                    }
                }, type = 1
            )
            viewPagerImageVideo.adapter = postContentAdapter
            postContentAdapter.update(
                post.postContent!!
            )
            followTV.isVisible= !viewModel.otherUsersId.contains(post.creatorId)
        }
    }

    private fun bindObservers() {
        viewModel.postDetails.observe(viewLifecycleOwner) {
            if (it.first.postId == post?.postId) {
                progressDialog.dismiss()
                if (post != it.first) {
                    post = it.first
                    exoPlayerItems.forEach { exo->
                        exo.exoPlayer.pause()
                        exo.exoPlayer.stop()
                        exo.exoPlayer.clearMediaItems()
                    }
                    initializePostData(post!!)
                }
                if (it.second.isNotEmpty())
                    binding.likeProfiles.isVisible = true
                if (it.third.isNotEmpty())
                    binding.commentProfiles.isVisible = true
                likeAdapter.submitList(it.second)
                commentsAdapter.submitList(it.third)
            }
        }
        viewModel.onlyUserDetail.observe(viewLifecycleOwner) {
            if (requestFromHere) {
                progressDialog.dismiss()
                it?.let {
                    val bundle = Bundle()
                    bundle.putSerializable("searchUser", it)
                    findNavController().navigate(
                        R.id.action_postDetailFragment_to_profileFragment,
                        args = bundle
                    )
                }
                requestFromHere= false
            }
        }
    }

    private fun showLikeAnimation() {
        binding.apply {
            likeAnimation.visibility = View.VISIBLE
            likeAnimation.playAnimation()
            Handler(Looper.getMainLooper()).postDelayed({
                likeAnimation.cancelAnimation()
                likeAnimation.visibility = View.GONE
            }, 750)
        }
    }

    override fun onNewAddClick() {}

    override fun onClickToView(postImageVideoWithExoPlayer: PostImageVideo) {
        post?.apply {
            viewModel.addLike(
                PostEntity(
                    postId,
                    creatorId,
                    postDesc,
                    postTime,
                    postContent,
                    creatorName,
                    creatorProfileImage,
                    likeCount,
                    commentCount,
                    likedAndCommentByMe,
                    myComment
                )
            )
        }
        showLikeAnimation()
    }

    override fun onPause() {
        super.onPause()
        exoPlayerItems.forEach {
            it.exoPlayer.pause()
            it.exoPlayer.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (post!=null){
            initializePostData(post!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        exoPlayerItems.forEach {
            it.exoPlayer.stop()
            it.exoPlayer.clearMediaItems()
        }
    }

}