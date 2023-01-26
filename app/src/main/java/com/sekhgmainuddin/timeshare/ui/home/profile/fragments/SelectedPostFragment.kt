package com.sekhgmainuddin.timeshare.ui.home.profile.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.sekhgmainuddin.timeshare.ui.home.home.adapters.ImageVideoViewPagerAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.databinding.SelectedPostFromProfileBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedPostFragment : DialogFragment() {

    private var _binding: SelectedPostFromProfileBinding?= null
    private val binding: SelectedPostFromProfileBinding
        get() = _binding!!
    private var post: Post?= null
    private lateinit var adapter: ImageVideoViewPagerAdapter
    private val viewModel by activityViewModels<HomeViewModel>()
    private var addedTempPost= false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        _binding= SelectedPostFromProfileBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post= viewModel.postPassedToView.value
        if (post?.postContent?.size==1){
            post?.postContent?.add(PostImageVideo(0,"",""))
            addedTempPost= true
        }
        initialize()

    }

    private fun initialize() {
        post?.let {
            val post= PostEntity(it.postId, it.creatorId, it.postDesc, it.postTime, it.postContent, it.creatorName, it.creatorProfileImage, it.likeCount, it.commentCount, it.likedAndCommentByMe, it.myComment)
            adapter= ImageVideoViewPagerAdapter(requireContext(), post){

            }
            binding.apply {
                viewPagerImageVideo.adapter= adapter
                likeCount.text= it.likeCount.toString()
                commentCount.text= it.commentCount.toString()
                likeButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(),if (it.likedAndCommentByMe in intArrayOf(1,3)) R.drawable.liked_icon else R.drawable.love_icon))
                commentButton.imageTintList= ColorStateList.valueOf(requireContext().getColor(if (it.likedAndCommentByMe in intArrayOf(2,3)) R.color.orangePink else R.color.black))
            }
            Handler(Looper.getMainLooper()).postDelayed({
                binding.viewPagerImageVideo.setCurrentItem(1,true)
            }, 500)
            Handler(Looper.getMainLooper()).postDelayed({
                if (addedTempPost){
                    adapter.post.postContent?.removeAt(1)
                    adapter.notifyItemRemoved(1)
                }
                binding.viewPagerImageVideo.setCurrentItem(0,true)
                binding.progressBar.isVisible= false
            },2000)
        }
    }

    override fun onPause() {
        super.onPause()
        addedTempPost= false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
        viewModel.postPassedToView.postValue(null)
    }

}