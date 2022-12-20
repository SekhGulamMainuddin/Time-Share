package com.sekhgmainuddin.timeshare.ui.loginandsignup.signupfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentPhoneOTPBinding
import kotlinx.coroutines.*

class PhoneOTPFragment : Fragment() {

    private var _binding: FragmentPhoneOTPBinding? = null
    private val binding: FragmentPhoneOTPBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentPhoneOTPBinding.inflate(inflater)

        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerClickListeners()

    }

    private fun registerClickListeners() {

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_phoneOTPFragment_to_signUpFragment)
        }

        binding.sendOTPButton.setOnClickListener {
            if(binding.phoneEditText.text.toString().isNotEmpty() && binding.phoneEditText.isEnabled){
                binding.phoneEditText.isEnabled= false
                binding.phoneAnimation.setAnimation(R.raw.otp_animation)
                binding.phoneAnimation.playAnimation()
                binding.otpTitle.visibility= View.VISIBLE
                binding.otpDescription.visibility= View.VISIBLE
                binding.otpView.visibility= View.VISIBLE
                binding.otpTimeLeft.visibility= View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    for(i in 59 downTo 0){
                        withContext(Dispatchers.Main){
                            binding.otpTimeLeft.text= "00:${if (i>9) "$i" else "0$i"}"
                        }
                        delay(1000)
                    }
                }
                binding.sendOTPButton.text= "Continue"
            }
            else if(binding.phoneEditText.text.toString().isEmpty())
                Toast.makeText(requireContext(), "Enter valid Number", Toast.LENGTH_SHORT).show()
            else if(!binding.phoneEditText.isEnabled){
                Toast.makeText(requireContext(), "Verification Done", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}