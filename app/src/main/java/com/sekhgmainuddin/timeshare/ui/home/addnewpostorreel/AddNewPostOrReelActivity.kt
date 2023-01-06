package com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.databinding.ActivityAddNewPostOrReelBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.adapters.ImageVideoViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.adapters.onClick
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.getFileExtension
import com.sekhgmainuddin.timeshare.utils.Utils.isImageOrVideo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNewPostOrReelActivity : AppCompatActivity(), onClick {

    private lateinit var binding: ActivityAddNewPostOrReelBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var adapter: ImageVideoViewPagerAdapter
    private val imageVideoUriList= mutableListOf<PostImageVideo>(PostImageVideo(-1,"",""))
    private var currIndex= 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddNewPostOrReelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        registerClickListeners()
        bindObserver()

    }

    private fun initialize() {

        adapter= ImageVideoViewPagerAdapter(this, this)
        binding.viewPagerImageVideo.adapter= adapter
        adapter.update(imageVideoUriList)

        TabLayoutMediator(binding.tabLayout, binding.viewPagerImageVideo)
        {_,_ -> }.attach()

    }

    private fun registerClickListeners() {

        binding.postButton.setOnClickListener {
//            viewModel.addPost(imageVideoUriList, "Hello this is a testing post")
        }


    }

    fun bindObserver(){
        viewModel.addPostStatus.observe(this){
            when(it){
                is NetworkResult.Success-> {
                    if (it.data==true && it.statusCode==200)
                        Toast.makeText(this, "Post Uploaded Successfully", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error-> {
                    if (it.statusCode==500)
                        Toast.makeText(this, "Some error occurred while uploading the post", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading-> {

                }
            }
        }
    }

    private val galleryLaunch= registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null) {
            val isImageOrVideo= getFileExtension(it,this@AddNewPostOrReelActivity)?.isImageOrVideo()
            if (isImageOrVideo==0 || isImageOrVideo==1){
                imageVideoUriList.add(currIndex,
                    PostImageVideo(
                        isImageOrVideo,
                        if (isImageOrVideo==0) it.toString() else "",
                        if (isImageOrVideo==1) it.toString() else ""
                    ))
                currIndex++
                adapter.update(imageVideoUriList)
            }
            else{
                Toast.makeText(this, "File Format Not Supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewAddClick() {
        galleryLaunch.launch("image/* video/*")
    }

    override fun onClickToView(postImageVideo: PostImageVideo) {
        Toast.makeText(this, "Item Clicked", Toast.LENGTH_SHORT).show()
    }

}