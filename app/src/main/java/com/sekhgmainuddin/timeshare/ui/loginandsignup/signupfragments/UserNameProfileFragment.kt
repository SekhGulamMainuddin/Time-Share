package com.sekhgmainuddin.timeshare.ui.loginandsignup.signupfragments

import android.app.Dialog
import android.content.Intent
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
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentUserNameProfileBinding
import com.sekhgmainuddin.timeshare.ui.home.MainActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginSignUpViewModel
import com.sekhgmainuddin.timeshare.utils.NetworkResult

class UserNameProfileFragment : Fragment() {

    private var _binding: FragmentUserNameProfileBinding? = null
    private val binding: FragmentUserNameProfileBinding
        get() = _binding!!
    private var imageUri: Uri? = null
    private var imageBitmap: Bitmap? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val viewModel by activityViewModels<LoginSignUpViewModel>()
    private lateinit var snackBar: Snackbar
    private lateinit var progressDialog: Dialog
    private lateinit var email: String
    private lateinit var phone: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentUserNameProfileBinding.inflate(inflater)
        // Inflate the layout for this fragment
        email = requireArguments().getString("email","null")
        phone = requireArguments().getString("phone","null")
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        setupBottomSheetImageUploadDialog()
        registerClickListeners()
        bindObservers()
    }

    private fun setupBottomSheetImageUploadDialog(){
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
            cameraLaunch.launch()
        }
        gallery?.setOnClickListener {
            galleryLaunch.launch("image/*")
        }
        galleryTV?.setOnClickListener {
            galleryLaunch.launch("image/*")
        }

    }

    private val galleryLaunch= registerForActivityResult(ActivityResultContracts.GetContent()){
        binding.addProfilePic.visibility= View.VISIBLE
        binding.profileAnimation.visibility= View.INVISIBLE
        binding.addProfilePic.setImageURI(it)
        imageUri= it
    }

    private val cameraLaunch = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        binding.addProfilePic.visibility= View.VISIBLE
        binding.profileAnimation.visibility= View.INVISIBLE
        binding.addProfilePic.setImageBitmap(it)
        imageBitmap= it
    }

    private fun registerClickListeners(){
        binding.addPic.setOnClickListener {
            bottomSheetDialog.show()
        }
        binding.createAccountButton.setOnClickListener {
            if(binding.nameEditText.text.toString().isEmpty())
                binding.nameEditText.error= "Enter Name"
            else if(binding.bioEditText.text.toString().isEmpty())
                binding.bioEditText.error= "Add Some Bio"
            else if(binding.interestGroup.checkedChipIds.isEmpty())
                showSnackBar("Select your Interests")
            else{
                progressDialog.show()
                val interests= ArrayList<String>()
                for (i in binding.interestGroup.checkedChipIds){
                    interests.add(binding.interestGroup.findViewById<Chip>(i).text.toString())
                }
                viewModel.uploadNewUserDetail(email, phone, imageUri, imageBitmap, binding.nameEditText.text.toString(),
                        binding.bioEditText.text.toString(),binding.locEditText.text.toString(),
                        interests)
            }
        }
    }

    private fun bindObservers() {
        viewModel.newUserDetailUpload.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null) {
                        showSnackBar(it.data!!)
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
                is NetworkResult.Error -> {
                    when (it.code) {
                        400 -> {
                            showSnackBar(it.message.toString())
                        }
                        409 -> {
                            progressDialog.show()
                            showSnackBar(it.message.toString())
                        }
                    }
                }
                is NetworkResult.Loading -> {
                    progressDialog.show()
                }
            }
        }
    }

    private fun showSnackBar(message: String) {
        snackBar = Snackbar.make(requireActivity().findViewById(R.id.parentLayout), message, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}