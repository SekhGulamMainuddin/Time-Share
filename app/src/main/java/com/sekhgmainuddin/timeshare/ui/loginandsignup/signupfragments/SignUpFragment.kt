package com.sekhgmainuddin.timeshare.ui.loginandsignup.signupfragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentSignUpBinding
import com.sekhgmainuddin.timeshare.ui.MainActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginSignUpViewModel
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.isValidEmail
import com.sekhgmainuddin.timeshare.utils.Utils.isValidPassword
import com.sekhgmainuddin.timeshare.utils.Utils.slideVisibility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding: FragmentSignUpBinding
        get() = _binding!!
    private val viewModel by activityViewModels<LoginSignUpViewModel>()
    private lateinit var progressDialog: Dialog
    private lateinit var snackBar: Snackbar


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


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


    private fun bindObservers() {
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
                        findNavController().navigate(R.id.action_signUpFragment_to_userNameProfileFragment)
                    }
                }
                is NetworkResult.Error -> {
                    showSnackBar("Some Error Occurred")
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

}