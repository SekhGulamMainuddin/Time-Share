package com.sekhgmainuddin.timeshare.ui.home.chat

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devlomi.record_view.OnRecordListener
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.ActivityChatBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.adapters.ChatsAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.attachments.ImagePickerActivity
import com.sekhgmainuddin.timeshare.utils.Keys
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils
import com.sekhgmainuddin.timeshare.utils.Utils.getRandomIDInteger
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo
import com.sekhgmainuddin.timeshare.utils.agora.RtcTokenBuilder2
import com.sekhgmainuddin.timeshare.utils.enums.MessageType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChatActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

    private lateinit var binding: ActivityChatBinding
    private val viewModel by viewModels<ChatsViewModel>()
    private lateinit var imagePickerBottomSheet: ImagePickerActivity

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var giphy: GiphyDialogFragment

    private lateinit var chatAdapter: ChatsAdapter

    private var profileId: String? = null
    private var profile: User? = null
    private var profileActiveStatus: Long = -1
    private var mediaRecorder: MediaRecorder? = null
    private var audioPath = ""
    private var sTime = ""
    private lateinit var chatsDialog: Dialog
    private var selectedMessage: ChatEntity? = null
    private lateinit var appCertificate: String
    private lateinit var appId: String
    private var expirationTimeInSeconds = 3600
    private var token: String? = null
    private var uid: Int? = null
    private val tokenBuilder = RtcTokenBuilder2()
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appCertificate = Keys.getAppCertificateAgora()
        appId = Keys.getAppIdAgora()


        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        chatsDialog = Dialog(this)
        chatsDialog.setContentView(R.layout.chats_dialog)
        chatsDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        chatsDialog.findViewById<LinearLayout>(R.id.replyMessage).setOnClickListener {
            chatsDialog.dismiss()
        }
        chatsDialog.findViewById<LinearLayout>(R.id.forwardMessage).setOnClickListener {
            chatsDialog.dismiss()
        }
        chatsDialog.findViewById<LinearLayout>(R.id.deleteMessage).setOnClickListener {
            selectedMessage?.let { chat -> viewModel.deleteChat(chat) }
            chatsDialog.dismiss()
        }

        imagePickerBottomSheet = ImagePickerActivity()

        val bundle = intent.getBundleExtra("profileBundle")
        profileId = bundle?.getString("profileId")
        profile = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            bundle?.getSerializable("profile", User::class.java)
        else
            bundle?.getSerializable("profile") as User

        updateProfileData(
            User(
                name = profile?.name ?: "",
                imageUrl = profile?.imageUrl ?: "",
                activeStatus = profile?.activeStatus ?: -1
            )
        )
        loadData()
        registerClickListeners()
        bindObserver()

    }

    private fun makeVideoCall() {
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()
        uid = getRandomIDInteger()
        token = tokenBuilder.buildTokenWithUid(
            appId,
            appCertificate,
            "com.sekhgmainuddin.timeshare",
            uid!!,
            RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            timestamp,
            timestamp
        )
        viewModel.makeVideoCall(profile!!, token!!, uid!!)
    }

    fun updateProfileData(user: User) {
        binding.apply {
            profileName.text = profile?.name ?: ""
            Glide.with(this@ChatActivity).load(profile?.imageUrl ?: "")
                .placeholder(R.drawable.default_profile_pic)
                .into(profileImage)
            if (user.activeStatus != profileActiveStatus) {
                profileActiveStatus = user.activeStatus
                val status = when (profileActiveStatus) {
                    -1L, 0L -> ""
                    1L -> "Active"
                    else -> profileActiveStatus.getTimeAgo()!!
                }
                profileStatus.text = status
                profileStatus.isVisible = status == ""
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadData() {

        chatAdapter = ChatsAdapter(this, profile?.imageUrl ?: "", firebaseAuth.currentUser?.uid!!) {
            selectedMessage = it
            chatsDialog.show()
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatsRecyclerView.layoutManager = layoutManager
        binding.chatsRecyclerView.adapter = chatAdapter

        viewModel.getLatestChats(profileId!!)

    }

    fun registerClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }
            sendMessage.setOnClickListener {
                if (messageInputET.text.toString().isNotEmpty()) {
                    viewModel.sendMessage(
                        profileId!!, MessageType.TEXT,
                        messageInputET.text.toString().trim(), ""
                    )
                    messageInputET.text.clear()
                } else {
                    Toast.makeText(
                        this@ChatActivity,
                        "Message field is Empty.\nPlease add some message.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            videoCall.setOnClickListener {
                makeVideoCall()
                progressDialog.show()
            }
            messageInputET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        if (s.isNotEmpty()) {
                            sendMessage.isVisible = true
                            recordMessage.isVisible = false
                        } else {
                            sendMessage.isVisible = false
                            recordMessage.isVisible = true
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })
            recordMessage.setRecordView(recordView)
            recordView.setOnRecordListener(object : OnRecordListener {
                override fun onStart() {
                    if (!checkPermissionFromDevice()) {
                        attachFiles.visibility = View.INVISIBLE
                        gallery.visibility = View.INVISIBLE
                        messageInputET.visibility = View.INVISIBLE
                        recordView.visibility = View.VISIBLE
                        startRecord()
                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vibratorManager =
                                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                            vibratorManager.defaultVibrator
                        } else {
                            getSystemService(VIBRATOR_SERVICE) as Vibrator
                        }
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                100,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        requestPermission()
                    }
                }

                override fun onCancel() {
                    try {
                        mediaRecorder!!.reset()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFinish(recordTime: Long, limitReached: Boolean) {
                    attachFiles.visibility = View.VISIBLE
                    gallery.visibility = View.VISIBLE
                    messageInputET.visibility = View.VISIBLE
                    recordView.visibility = View.GONE

                    try {
                        sTime = getHumanTimeText(recordTime) ?: ""
                        stopRecord()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onLessThanSecond() {
                    attachFiles.visibility = View.VISIBLE
                    gallery.visibility = View.VISIBLE
                    messageInputET.visibility = View.VISIBLE
                    recordView.visibility = View.GONE
                }
            })
            recordView.setOnBasketAnimationEndListener {
                attachFiles.visibility = View.VISIBLE
                gallery.visibility = View.VISIBLE
                messageInputET.visibility = View.VISIBLE
                recordView.visibility = View.GONE
            }
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            gallery.setOnClickListener {
                imageVideoResult.launch(galleryIntent)
            }
            gif.setOnClickListener {
                giphy.show(supportFragmentManager, "giphy_dialog")
            }
            attachFiles.setOnClickListener {
                attachments.isVisible = !attachments.isVisible
            }

            val fileIntent = Intent(Intent.ACTION_PICK)
            fileIntent.type = "*/*"
            fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            fileIntent.action = Intent.ACTION_GET_CONTENT

            file.setOnClickListener {
                fileResult.launch(fileIntent)
            }

        }
    }

    private val imageVideoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.sendFiles()
        }
    private val fileResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.sendFiles()
        }

    private fun bindObserver() {
        viewModel.sendMessageStatus.observe(this) {
            when (it) {
                is NetworkResult.Success -> {}
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }

                is NetworkResult.Loading -> {

                }
            }
        }
        viewModel.chatList.observe(this) {
            chatAdapter.submitList(it)
            binding.chatsRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        }
        viewModel.callSuccess.observe(this) {
            it.onSuccess { c->
                if (c.isNotEmpty()){
                    progressDialog.dismiss()
                    startActivity(
                        Intent(this, VideoCallActivity::class.java)
                            .putExtra("agoraToken", token)
                            .putExtra("uid", uid)
                            .putExtra("profileId", profileId)
                            .putExtra("byMe", true)
                            .putExtra("callId", c)
                    )
                }
            }
            it.onFailure {
                Toast.makeText(
                    this,
                    "Failed to Do Video Call. Some Error Occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun stopRecord() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
//                chatService.sendVoice(audioPath)
                Toast.makeText(this, audioPath, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                "Stop Recording Error :" + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getHumanTimeText(milliseconds: Long): String? {
        return String.format(
            "%02d", TimeUnit.MILLISECONDS.toSeconds(milliseconds) + TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            )
        )
    }

    private fun startRecord() {
        setUpMediaRecorder()
        try {
            if (mediaRecorder != null) {
                mediaRecorder!!.prepare()
                mediaRecorder!!.start()
            }
        } catch (e: Exception) {
            Log.d("recordException", "startRecord: $e")
        }
    }

    private fun setUpMediaRecorder() {
        val path_save =
            Environment.getExternalStorageDirectory().absolutePath + "/" + UUID.randomUUID()
                .toString() + "audio_record.m4a"
        audioPath = path_save
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder!!.setOutputFile(path_save)
        } catch (e: Exception) {
            Log.d("recorderException", "setUpMediaRecord: " + e.message)
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ),
            332
        )
    }

    private fun requestPermissionAboveAndroid11() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.action = Manifest.permission.RECORD_AUDIO
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                332
            )
        }
    }

    private fun checkPermissionFromDevice(): Boolean {
        val write_external_storage_result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val record_audio_result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return write_external_storage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED
    }

    override fun didSearchTerm(term: String) {}
    override fun onDismissed(selectedContentType: GPHContentType) {}
    override fun onGifSelected(
        media: Media,
        searchTerm: String?,
        selectedContentType: GPHContentType
    ) {
        media.url?.let {
            val gifUrl =
                "https://i.giphy.com/media/${it.substring(it.lastIndexOf('-') + 1)}/200.gif"
            viewModel.sendFileMessage(null, gifUrl, MessageType.GIF, profileId!!)
        }
    }

    private fun ActivityResult?.sendFiles() {
        this?.let {
            if (this@sendFiles.data?.clipData != null) {
                this@sendFiles.data?.clipData?.let { data ->
                    val list = ArrayList<Uri>()
                    for (i in 0 until data.itemCount) {
                        list.add(data.getItemAt(i).uri)
                    }
                    viewModel.sendMultipleFileMessage(list, profileId!!)
                }
            } else if (this@sendFiles.data?.data != null) {
                viewModel.sendFileMessage(this@sendFiles.data?.data, "", null, profileId!!)
            } else {
                //do nothing
            }
        }
    }

}