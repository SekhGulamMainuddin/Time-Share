package com.sekhgmainuddin.timeshare.ui.home.chat.ui

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityVideoCallBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.utils.Keys
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    private var callId: String? = null
    private var appId: String? = null
    private var appCertificate: String? = null
    private var token: String? = null
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null
    private var oppositeProfileId: String? = null
    private var oppositeProfileImage: String? = null
    private var oppositeProfileName: String? = null
    private var byMe: Boolean? = null
    private val viewModel by viewModels<ChatsViewModel>()
    private val homeViewModel by viewModels<HomeViewModel>()
    private var mMuted: Boolean = false
    private lateinit var progressDialog: Dialog
    private var typeVideo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        appId = Keys.getAppIdAgora()
        appCertificate = Keys.getAppCertificateAgora()
        callId = intent.getStringExtra("callId")
        oppositeProfileId = intent.getStringExtra("profileId")
        oppositeProfileImage = intent.getStringExtra("profileImage")
        oppositeProfileName = intent.getStringExtra("profileName")
        token = intent.getStringExtra("agoraToken")
        byMe = intent.getBooleanExtra("byMe", false)
        typeVideo = intent.getBooleanExtra("typeVideo", false)

        Glide.with(this).load(oppositeProfileImage).placeholder(R.color.black)
            .into(binding.oppositeProfileImage)
        binding.oppositeProfileName.text =
            "Call ${if (byMe == true) "to" else "from"} $oppositeProfileName"

        if (!typeVideo){
            binding.apply {
                callingAnimation.setAnimation(R.raw.phone_call_animation)
                localVideoViewContainer.isVisible= false
                remoteVideoViewContainer.isVisible= false
            }

        }

        if(typeVideo){
            if (!checkSelfPermissionVideo()) {
                ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
            }
        }else{
            if (!checkSelfPermissionAudio()) {
                ActivityCompat.requestPermissions(this, arrayOf(REQUESTED_PERMISSIONS[0]), PERMISSION_REQ_ID)
            }
        }

        setupCallSDKEngine()

        if (byMe!!) {
            showAndHide(true)
            joinChannel()
        } else {
            showAndHide(false)
            binding.endCall.isVisible = true
        }

        registerClickListeners()
        bindObservers()
        viewModel.observeCall()

        agoraEngine!!.muteLocalAudioStream(false)

    }

    private fun showAndHide(show: Boolean) {
        binding.apply {
            acceptCall.isVisible = !show
            endCall.isVisible = show
            micOnOff.isVisible = show
            switchCamera.isVisible = show && typeVideo
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
                homeViewModel.changeCallStatus(mMuted)
                localMicOff.isVisible= mMuted
                micOnOff.setImageResource(res)
            }
            switchCamera.setOnClickListener {
                agoraEngine?.switchCamera()
            }
        }
    }

    private fun bindObservers() {
        viewModel.callStatus.observe(this) {
            it.onSuccess { call ->
                binding.remoteMicOff.isVisible= call.oppositeMicStatus
                if (call.oppositeMicStatus){
                    Toast.makeText(this, "User Muted", Toast.LENGTH_SHORT).show()
                }
                if (call.uid == -1) {
                    progressDialog.show()
                    viewModel.deleteCall(oppositeProfileId!!)
                }
            }
        }
        viewModel.deleteCallResult.observe(this) {
            progressDialog.dismiss()
            finish()
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                setupRemoteVideo(uid)
                binding.apply {
                    callingAnimation.isVisible = false
                    oppositeProfileImage.isVisible = false
                    oppositeProfileName.isVisible = false
                }
            }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                remoteSurfaceView!!.visibility = View.GONE
                if (reason == 0) {
                    progressDialog.show()
                    viewModel.deleteCall(oppositeProfileId!!)
                }
            }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = binding.remoteVideoViewContainer
        remoteSurfaceView = SurfaceView(baseContext)
        container.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
    }

    private fun joinChannel() {
        if (checkSelfPermissionVideo()) {
            val options = ChannelMediaOptions()
            if (typeVideo){
                options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                setupLocalVideo()
                localSurfaceView!!.isVisible = true
                agoraEngine!!.startPreview()
            }
            else{
                options.autoSubscribeAudio = true
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            }
            agoraEngine!!.joinChannel(token, callId, 0, options)
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun leaveChannel() {
        if (isJoined) {
            agoraEngine!!.leaveChannel()
            if (typeVideo){
                if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
                if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
                isJoined = false
            }
        }
        viewModel.deleteCall(oppositeProfileId!!)
        progressDialog.show()
    }


    private fun setupLocalVideo() {
        val container = binding.localVideoViewContainer
        localSurfaceView = SurfaceView(baseContext)
        localSurfaceView!!.setZOrderMediaOverlay(true)
        container.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
    }


    private fun setupCallSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            if (typeVideo)
                agoraEngine?.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }


    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private fun checkSelfPermissionVideo(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    REQUESTED_PERMISSIONS[1]
                ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun checkSelfPermissionAudio(): Boolean {
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
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

}