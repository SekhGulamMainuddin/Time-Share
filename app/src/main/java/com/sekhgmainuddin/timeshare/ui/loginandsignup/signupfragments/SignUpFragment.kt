package com.sekhgmainuddin.timeshare.ui.loginandsignup.signupfragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentSignUpBinding
import com.sekhgmainuddin.timeshare.ui.home.MainActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginSignUpViewModel
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.isValidEmail
import com.sekhgmainuddin.timeshare.utils.Utils.isValidPassword
import com.sekhgmainuddin.timeshare.utils.Utils.slideVisibility
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding: FragmentSignUpBinding
        get() = _binding!!
    private val viewModel by activityViewModels<LoginSignUpViewModel>()
    private lateinit var progressDialog: Dialog
    private lateinit var snackBar: Snackbar
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSignUpBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerClickListeners()
        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindObservers()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

    }

    private fun registerClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().finish()
        }
        binding.createAccountButton.setOnClickListener {
            if (!binding.emailEditText.text.toString().isValidEmail())
                binding.emailEditText.error = "Enter the valid Email"
            else if (!binding.passwordToggleLayout.isVisible)
                binding.passwordToggleLayout.slideVisibility(true, 500)
            else {
                if (binding.passwordEditText.text.toString().isValidPassword()) {
                    progressDialog.show()
                    viewModel.signUpEmailPassword(
                        binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()
                    )
                } else {
                    binding.passwordToggleLayout.error = resources.getString(R.string.password_pattern)
                }
            }
        }
        binding.phoneSignUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_phoneOTPFragment)
        }

        binding.googleSignUpButton.setOnClickListener {
            progressDialog.show()
            startGoogleSignIn.launch(mGoogleSignInClient.signInIntent)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


    private fun bindObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner){
            progressDialog.dismiss()
            when(it){
                is NetworkResult.Success -> {
                    if (it.data!=null && it.code==201) {
                        val bundle= Bundle()
                        bundle.putString("email", it.data!!.email ?: " ")
                        bundle.putString("phone", it.data!!.phoneNumber ?: " ")
                        findNavController().navigate(R.id.action_signUpFragment_to_userNameProfileFragment, bundle)
                    }
                    else if (it.data!=null && it.code==200){
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
                is NetworkResult.Error -> {
                    showSnackBar("Some Error Occurred")
                    progressDialog.dismiss()
                }
                is NetworkResult.Loading -> {
                    progressDialog.dismiss()
                }
            }
        }
        viewModel.signUpResult.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null) {
                        snackBar = Snackbar.make(
                            requireActivity().findViewById(R.id.parentLayout),
                            "Sign Up Successful",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.show()
                        val bundle = Bundle()
                        bundle.putString("email", it.data!!.email)
                        findNavController().navigate(R.id.action_signUpFragment_to_userNameProfileFragment, bundle)
                    }
                }
                is NetworkResult.Error -> {
                    when(it.code){
                        500 -> {
                            showSnackBar("Server Error Occurred. Please check your Internet Connection")
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

    private val startGoogleSignIn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    viewModel.googleLogin(credential)
                } catch (e: ApiException) {
                    progressDialog.dismiss()
                    showSnackBar("Failed to Sign In")
                }
            }
        }


}