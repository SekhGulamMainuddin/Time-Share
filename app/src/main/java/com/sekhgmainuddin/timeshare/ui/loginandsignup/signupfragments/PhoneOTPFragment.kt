package com.sekhgmainuddin.timeshare.ui.loginandsignup.signupfragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentPhoneOTPBinding
import com.sekhgmainuddin.timeshare.ui.home.MainActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginSignUpViewModel
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class PhoneOTPFragment : Fragment() {

    private var _binding: FragmentPhoneOTPBinding? = null
    private val binding: FragmentPhoneOTPBinding
        get() = _binding!!
    private lateinit var progressDialog: Dialog
    private lateinit var mVerificationId: String
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    private val viewModel by activityViewModels<LoginSignUpViewModel>()
    private lateinit var snackBar: Snackbar
    private var loginIntent: Boolean= false

    @Inject
    lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentPhoneOTPBinding.inflate(inflater)
        loginIntent= arguments?.getBoolean("phone", false) == true
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        registerClickListeners()
        binding.otpView.visibility= View.VISIBLE
        bindObservers()
    }

    private fun registerClickListeners() {

        binding.backButton.setOnClickListener {
            if(loginIntent) {
                requireActivity().finish()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
            else
                findNavController().popBackStack()
        }

        binding.sendOTPButton.setOnClickListener {
            if(binding.phoneEditText.text.toString().isNotEmpty() && binding.phoneEditText.isEnabled && !binding.otpView.isVisible){
                binding.phoneEditText.isEnabled= false
                binding.phoneAnimation.setAnimation(R.raw.otp_animation)
                binding.phoneAnimation.playAnimation()
                binding.otpTitle.visibility= View.VISIBLE
                binding.otpDescription.visibility= View.VISIBLE
                binding.otpView.visibility= View.VISIBLE
                progressDialog.show()
                startPhoneNumberVerification(binding.phoneEditText.text.toString())
            }
            else if(binding.phoneEditText.text.toString().isEmpty())
                Toast.makeText(requireContext(), "Enter valid Number", Toast.LENGTH_SHORT).show()
            else if(binding.otpView.isVisible && binding.otpView.text.toString().length==6){
                progressDialog.show()
                verifyPhoneNumberWithCode(mVerificationId, binding.otpView.text.toString())
            }else{
                binding.otpView.error= "Enter the OTP"
                Toast.makeText(requireContext(), binding.otpView.text.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        binding.resendButton.setOnClickListener {
            resendVerificationCode(binding.phoneEditText.text.toString(), mResendToken)
        }
    }

    fun bindObservers(){
        viewModel.phoneLoginSignUp.observe(viewLifecycleOwner){
            progressDialog.dismiss()
            when(it){
                is NetworkResult.Success -> {
                    otpTimer.cancel()
                    if (it.code==200){
                        showSnackBar("Logged in successfully")
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                    else if (it.code==201){
                        showSnackBar("Sign-Up Successful")
                        val bundle= Bundle()
                        bundle.putString("phone", binding.phoneEditText.text.toString())
                        findNavController().navigate(R.id.action_phoneOTPFragment_to_userNameProfileFragment, bundle)
                    }

                }
                is NetworkResult.Error -> {

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

    private val otpTimer = object : CountDownTimer(120000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.otpTimeLeft.text = if(millisUntilFinished>60000) "0"+SimpleDateFormat("M:ss").format(Date(millisUntilFinished)) else "00"+SimpleDateFormat(":ss").format(Date(millisUntilFinished))
        }
        override fun onFinish() {
            binding.otpTimeLeft.text = "00:00"
            binding.phoneEditText.isEnabled= true
            binding.resendButton.visibility= View.VISIBLE
        }
    }

    private val mCallbacks= object : OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            viewModel.phoneLoginSignUp(credential)
            progressDialog.dismiss()
        }

        override fun onVerificationFailed(e: FirebaseException) {
            progressDialog.dismiss()
            if (e is FirebaseAuthInvalidCredentialsException) {
                //Toast.makeText(requireContext(), "Verification Failed due to Invalid Number", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(requireContext(), "Too Many Requests Generated. Please try after some time.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: ForceResendingToken
        ) {
            binding.otpTimeLeft.visibility= View.VISIBLE
            otpTimer.start()
            mVerificationId = verificationId
            mResendToken = token
            binding.sendOTPButton.text = resources.getString(R.string.continue_)
            progressDialog.dismiss()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(120L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(120L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(mCallbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        viewModel.phoneLoginSignUp(credential)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}