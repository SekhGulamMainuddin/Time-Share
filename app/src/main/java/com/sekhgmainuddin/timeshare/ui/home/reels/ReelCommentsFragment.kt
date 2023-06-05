package com.sekhgmainuddin.timeshare.ui.home.reels

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.CommentWithProfile
import com.sekhgmainuddin.timeshare.data.modals.LikeWithProfile
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.FragmentReelCommentsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters.CommentAdapter
import com.sekhgmainuddin.timeshare.ui.home.postdetail.adapters.LikeAdapter
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReelCommentsFragment(private val reel: Reel): BottomSheetDialogFragment(){

    private var _binding: FragmentReelCommentsBinding?= null
    private val binding: FragmentReelCommentsBinding
        get() = _binding!!
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var likeAdapter: LikeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentReelCommentsBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerClickListeners()
        initialize()
        bindObservers()
    }

    private fun registerClickListeners(){
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun initialize(){
        viewModel.getCommentsAndLikesByReelsId(reel.reelId)
        Glide.with(requireContext()).load(reel.creatorImageUrl).placeholder(R.drawable.default_profile_pic).into(binding.addCommentProfileImage)
        if (reel.myComment.isNotEmpty())
            binding.commentTextAdd.setText(reel.myComment)
        commentAdapter= CommentAdapter(requireContext())
        likeAdapter= LikeAdapter(requireContext())
        binding.commentsRecyclerView.adapter= commentAdapter
        binding.likesRecyclerView.adapter= likeAdapter
        commentAdapter.submitList(listOf(CommentWithProfile(commentTime = -1)))
        likeAdapter.submitList(listOf(LikeWithProfile(likedTime = -1)))
        binding.commentTextAdd.setOnKeyListener(object: View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if ((event?.action == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (binding.commentTextAdd.text.isEmpty())
                        Toast.makeText(requireContext(), "Add some comment", Toast.LENGTH_SHORT).show()
                    else {
                        viewModel.commentReel(reel.reelId, binding.commentTextAdd.text.toString())
                        hideKeyboard()
                    }
                    return true
                }
                return false
            }
        })
    }

    private fun bindObservers(){
        viewModel.reelDetails.observe(viewLifecycleOwner){
            it.onSuccess { reelDetail->
                Handler(Looper.getMainLooper()).postDelayed({commentAdapter.submitList(reelDetail.second)
                    likeAdapter.submitList(reelDetail.first)},2000)
            }
            it.onFailure {
                Toast.makeText(requireContext(), "Failed to access Comments and Likes", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.commentReelStatus.observe(viewLifecycleOwner){
            binding.commentTextAdd.isEnabled= true
            when(it){
                is NetworkResult.Success->{
                    binding.commentProgress.indeterminateTintList= ColorStateList.valueOf(requireContext().getColor(R.color.green))
                    Toast.makeText(requireContext(), "Comment Posted", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.commentProgress.isVisible= false
                    },1250)
                }
                is NetworkResult.Error->{
                    Toast.makeText(requireContext(), "Failed to add the comment", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading->{
                    binding.commentTextAdd.isEnabled= false
                }
            }
        }
        
        
        
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(object :
            BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //In the EXPANDED STATE apply a new MaterialShapeDrawable with rounded corners
                    val newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet)
                    ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return dialog
    }

    private fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable {
        val shapeAppearanceModel =
            //Create a ShapeAppearanceModel with the same shapeAppearanceOverlay used in the style
            ShapeAppearanceModel.builder(
                requireContext(),
                0,
                R.style.CustomShapeAppearanceBottomSheetDialog
            )
                .build()

        //Create a new MaterialShapeDrawable (you can't use the original MaterialShapeDrawable in the BottomSheet)
        val currentMaterialShapeDrawable = bottomSheet.background as MaterialShapeDrawable
        val newMaterialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        //Copy the attributes in the new MaterialShapeDrawable
        newMaterialShapeDrawable.initializeElevationOverlay(requireContext())
        newMaterialShapeDrawable.fillColor = currentMaterialShapeDrawable.fillColor
        newMaterialShapeDrawable.tintList = currentMaterialShapeDrawable.tintList
        newMaterialShapeDrawable.elevation = currentMaterialShapeDrawable.elevation
        newMaterialShapeDrawable.strokeWidth = currentMaterialShapeDrawable.strokeWidth
        newMaterialShapeDrawable.strokeColor = currentMaterialShapeDrawable.strokeColor
        return newMaterialShapeDrawable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("reelcommentsFragment", "onDestroyView: Destroyed")
        _binding= null
    }

}