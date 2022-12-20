package com.sekhgmainuddin.timeshare.ui.loginandsignup.signupfragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentUserNameProfileBinding
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginSignUpViewModel

class UserNameProfileFragment : Fragment() {

    private var _binding: FragmentUserNameProfileBinding? = null
    private val binding: FragmentUserNameProfileBinding
        get() = _binding!!
    var imageUri: Uri? = null
    var imageBitmap: Bitmap? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val viewModel by activityViewModels<LoginSignUpViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentUserNameProfileBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetImageUploadDialog()
        registerClickListeners()

    }

    fun setupBottomSheetImageUploadDialog(){
        bottomSheetDialog= BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.image_upload_dialog)
        val camera= bottomSheetDialog.findViewById<ImageButton>(R.id.camera)
        val cameraTV= bottomSheetDialog.findViewById<TextView>(R.id.cameraTV)
        val gallery= bottomSheetDialog.findViewById<ImageButton>(R.id.gallery)
        val galleryTV= bottomSheetDialog.findViewById<TextView>(R.id.galleryTV)

        camera?.setOnClickListener {
            cameraLaunch.launch()
        }
        cameraTV?.setOnClickListener {

        }
        gallery?.setOnClickListener {
            galleryLaunch.launch("image/*")
        }
        galleryTV?.setOnClickListener {

        }

    }

    val galleryLaunch= registerForActivityResult(ActivityResultContracts.GetContent()){
        binding.addProfilePic.visibility= View.VISIBLE
        binding.profileAnimation.visibility= View.INVISIBLE
        binding.addProfilePic.setImageURI(it)
        imageUri= it
    }

    val cameraLaunch = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        binding.addProfilePic.visibility= View.VISIBLE
        binding.profileAnimation.visibility= View.INVISIBLE
        binding.addProfilePic.setImageBitmap(it)
        imageBitmap= it
    }

    fun registerClickListeners(){
        binding.addPic.setOnClickListener {
            bottomSheetDialog.show()
        }
        binding.createAccountButton.setOnClickListener {
            viewModel.uploadNewUserDetail(imageUri,imageBitmap,"My Name is Khan",
                "jahgsdakjgdakjd","India","[adhslkd]")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}