package com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Log.println
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sekhgmainuddin.timeshare.databinding.ActivityGroupCallBinding
import com.sekhgmainuddin.timeshare.utils.Keys
import io.agora.agorauikit_android.AgoraButton
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraSettings
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.agorauikit_android.requestPermission
import io.agora.rtc2.Constants

@ExperimentalUnsignedTypes
class GroupCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupCallBinding
    private var agView: AgoraVideoViewer? = null
    private lateinit var appId: String
    private lateinit var channelName: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appId = Keys.getAppIdAgora()
        channelName = intent.getStringExtra("callId")!!
        token = intent.getStringExtra("agoraToken")!!

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        } else {
            try {
                agView = AgoraVideoViewer(
                    this, AgoraConnectionData(appId),
                    agoraSettings = this.settingsWithExtraButtons()
                )
            } catch (e: Exception) {
                println(
                    Log.ERROR,
                    "VideoUIKit App",
                    "Could not initialise AgoraVideoViewer. Check your App ID is valid. ${e.message}"
                )
                return
            }
            try {
                val set = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                this.addContentView(agView, set)
                if (AgoraVideoViewer.requestPermission(this)) {
                    agView!!.join(channelName, token, role = Constants.CLIENT_ROLE_BROADCASTER, 0)
                } else {
                    val joinButton = Button(this)
                    joinButton.text = "Allow Camera and Microphone, then click here"
                    joinButton.setOnClickListener {
                        if (AgoraVideoViewer.requestPermission(this)) {
                            (joinButton.parent as ViewGroup).removeView(joinButton)
                            agView!!.join(channelName, token, role = Constants.CLIENT_ROLE_BROADCASTER, 0)
                        }
                    }
                    joinButton.setBackgroundColor(Color.GREEN)
                    joinButton.setTextColor(Color.RED)
                    this.addContentView(
                        joinButton,
                        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300)
                    )
                }
            }catch (e: Exception){
                Log.d("groupVideoCallException", "onCreate: $e")
            }
        }
    }


    fun settingsWithExtraButtons(): AgoraSettings {
        val agoraSettings = AgoraSettings()

        val agBeautyButton = AgoraButton(this)
        agBeautyButton.clickAction = {
            it.isSelected = !it.isSelected
            agBeautyButton.setImageResource(
                if (it.isSelected) android.R.drawable.star_on else android.R.drawable.star_off
            )
            it.background.setTint(if (it.isSelected) Color.GREEN else Color.GRAY)
            this.agView?.agkit?.setBeautyEffectOptions(it.isSelected, this.agView?.beautyOptions)
        }
        agBeautyButton.setImageResource(android.R.drawable.star_off)

        agoraSettings.extraButtons = mutableListOf(agBeautyButton)

        return agoraSettings
    }

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
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

}
