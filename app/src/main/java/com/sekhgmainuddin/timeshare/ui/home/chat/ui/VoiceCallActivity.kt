package com.sekhgmainuddin.timeshare.ui.home.chat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityVoiceCallBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.utils.Keys
import io.agora.rtc2.*

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    private val viewModel by viewModels<ChatsViewModel>()
    private var callId: String? = null
    private var appId: String? = null
    private var appCertificate: String? = null
    private var token: String? = null
    private var uid: Int? = null
    private var oppositeProfileId: String? = null
    private var byMe: Boolean? = null
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var mMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appId = Keys.getAppIdAgora()
        appCertificate = Keys.getAppCertificateAgora()
        callId = intent.getStringExtra("callId")
        oppositeProfileId = intent.getStringExtra("profileId")
        token = intent.getStringExtra("agoraToken")
        uid = intent.getIntExtra("uid", 0)
        byMe = intent.getBooleanExtra("byMe", false)

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }

        registerClickListeners()
        setupVoiceSDKEngine()
        bindObservers()
        viewModel.observeCall()

        if (byMe!!){
            showAndHide(true)
            joinChannel()
        } else {
            showAndHide(false)
            binding.endCall.isVisible= true
        }
    }

    private fun registerClickListeners() {
        binding.apply {
            acceptCall.setOnClickListener {
                joinChannel()
                showAndHide(true)
            }
            endCall.setOnClickListener {
                leaveChannel()
            }
            micOnOff.setOnClickListener {
                mMuted = !mMuted
                agoraEngine!!.muteLocalAudioStream(mMuted)
                val res: Int = if (mMuted) {
                    R.drawable.baseline_mic_off_24
                } else {
                    R.drawable.baseline_mic_24
                }
                micOnOff.setImageResource(res)
            }
        }
    }

    private fun showAndHide(show: Boolean){
        binding.apply {
            acceptCall.isVisible= !show
            endCall.isVisible= show
            micOnOff.isVisible= show
        }
    }

    private fun bindObservers() {
        viewModel.callStatus.observe(this){
            it.onSuccess { call ->
                if (call.uid==-1){
                    viewModel.deleteCall(callId!!)
                }
            }
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("User Connected")
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Waiting For User")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("User Disconnected")
        }

        override fun onLeaveChannel(stats: RtcStats) {
            isJoined = false
        }
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions()
        options.autoSubscribeAudio = true
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        agoraEngine!!.joinChannel(token, callId, uid!!, options)
    }

    private fun leaveChannel() {
        agoraEngine!!.leaveChannel()
        viewModel.deleteCall(callId!!)
    }

    private fun setupVoiceSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            throw RuntimeException("Check the error.")
        }
    }

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }


}