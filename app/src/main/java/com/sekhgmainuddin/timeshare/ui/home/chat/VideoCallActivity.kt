package com.sekhgmainuddin.timeshare.ui.home.chat

import android.Manifest
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
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityVideoCallBinding
import com.sekhgmainuddin.timeshare.utils.Keys
import com.sekhgmainuddin.timeshare.utils.ManagePermissions
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var rtcEngine: RtcEngine
    private lateinit var permissionManager: ManagePermissions
    private var callId: String? = null
    private var appId: String? = null
    private var appCertificate: String? = null
    private val channelName = "com.sekhgmainuddin.timeshare"
    private var token: String? = null
    private var uid: Int? = null
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null
    private var oppositeProfileId: String? = null
    private var byMe: Boolean? = null
    private val viewModel by viewModels<ChatsViewModel>()
    private var mMuted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appId = Keys.getAppIdAgora()
        appCertificate = Keys.getAppCertificateAgora()
        callId= intent.getStringExtra("callId")
        oppositeProfileId= intent.getStringExtra("profileId")
        token= intent.getStringExtra("agoraToken")
        uid= intent.getIntExtra("uid", 0)
        byMe= intent.getBooleanExtra("byMe", false)

        permissionManager = ManagePermissions(
            this,
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
            101
        )

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }

        setupVideoSDKEngine()

        if (byMe!!){
            showAndHide(true)
            joinChannel()
        } else {
            showAndHide(false)
        }

        registerClickListeners()
        bindObservers()
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

    private fun showAndHide(show: Boolean){
        binding.apply {
            acceptCall.isVisible= !show
            endCall.isVisible= show
            micOnOff.isVisible= show
            switchCamera.isVisible= show
        }
    }

    private fun registerClickListeners(){
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
                rtcEngine.muteLocalAudioStream(mMuted)
                val res: Int = if (mMuted) {
                    R.drawable.baseline_mic_off_24
                } else {
                    R.drawable.baseline_mic_24
                }
                micOnOff.setImageResource(res)
            }
            switchCamera.setOnClickListener {
                agoraEngine?.switchCamera()
            }
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("User Disconnected because $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = binding.remoteVideoViewContainer
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView?.setZOrderMediaOverlay(true)
        container.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        remoteSurfaceView?.visibility = View.VISIBLE
    }

    private fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, channelName, uid!!, options)
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun leaveChannel() {
        if (!isJoined) {
            showMessage("Join a call first")
        } else {
            agoraEngine!!.leaveChannel()
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
            viewModel.deleteCall(callId!!)
            finish()
        }
    }


    private fun setupLocalVideo() {
        val container = binding.localVideoViewContainer
        localSurfaceView = SurfaceView(baseContext)
        container.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                uid!!
            )
        )
    }


    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
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

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    REQUESTED_PERMISSIONS[1]
                ) != PackageManager.PERMISSION_GRANTED)
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