package com.sekhgmainuddin.timeshare.ui.home.chat.ui

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devlomi.record_view.OnRecordListener
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentChatBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.ChatsAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.GroupChatsAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.GroupCallActivity
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.singlechat.CallActivity
import com.sekhgmainuddin.timeshare.utils.Keys
import com.sekhgmainuddin.timeshare.utils.NetworkResult
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
class ChatsFragment : Fragment() {

    companion object{
        var currentChatId = ""
    }

    private var _binding: FragmentChatBinding? = null
    private val binding: FragmentChatBinding
        get() = _binding!!
    private val viewModel by activityViewModels<ChatsViewModel>()
    private lateinit var imagePickerBottomSheet: ImagePickerActivity

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var giphy: GiphyDialogFragment

    private lateinit var chatAdapter: ChatsAdapter
    private lateinit var groupChatsAdapter: GroupChatsAdapter
    private val tokenBuilder = RtcTokenBuilder2()
    private var profileId: String? = null
    private var profile: User? = null
    private var isGroup: Boolean = false
    private var profileGroup: GroupEntity? = null
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
    private var callId: String? = null
    private lateinit var progressDialog: Dialog
    private lateinit var chatId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appCertificate = Keys.getAppCertificateAgora()
        appId = Keys.getAppIdAgora()

        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        chatsDialog = Dialog(requireContext())
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

        val bundle = requireArguments()
        isGroup = bundle.getBoolean("isGroup", false)
        profileId = bundle.getString("profileId")
        if (isGroup) {
            profileGroup = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                bundle.getSerializable("profile", GroupEntity::class.java)
            else
                bundle.getSerializable("profile") as GroupEntity
            updateProfileData()
        } else {
            profile = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                bundle.getSerializable("profile", User::class.java)
            else
                bundle.getSerializable("profile") as User
            updateProfileData()
        }
        chatId = getChatId()!!
        currentChatId= chatId

        loadData()
        registerClickListeners()
        bindObserver()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeChatListeners(currentChatId)
        currentChatId= ""
        _binding = null
    }

    private fun makeCall(typeVideoOrVoice: Boolean) {
        callId = UUID.randomUUID().toString()
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()
        token = tokenBuilder.buildTokenWithUid(
            appId,
            appCertificate,
            callId,
            0,
            RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            timestamp,
            timestamp
        )
        viewModel.makeCall(profile, token!!, 0, typeVideoOrVoice, callId!!, profileGroup)

    }

    fun updateProfileData() {
        binding.apply {
            profileName.text = if (profile != null) profile?.name else profileGroup?.groupName
            Glide.with(this@ChatsFragment)
                .load(if (profile == null) profileGroup?.groupImageUrl else profile?.imageUrl)
                .placeholder(R.drawable.default_profile_pic)
                .into(profileImage)
            if (profile == null) {
                val userList = profileGroup?.groupMembers?.values?.map {
                    return@map it.name.substring(
                        0,
                        it.name.lastIndexOf(' ')
                    )
                }.toString()
                profileStatus.text = userList.substring(1, userList.length - 1)
            } else {
                if (profile?.activeStatus != profileActiveStatus) {
                    profileActiveStatus = profile?.activeStatus!!
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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadData() {
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatsRecyclerView.layoutManager = layoutManager
        if (isGroup) {
            groupChatsAdapter = GroupChatsAdapter(
                profileGroup!!.groupMembers,
                requireContext(),
                firebaseAuth.currentUser?.uid!!
            ) {
                selectedMessage = it
                chatsDialog.show()
            }
            binding.chatsRecyclerView.adapter = groupChatsAdapter
        } else {
            chatAdapter = ChatsAdapter(requireContext(), firebaseAuth.currentUser?.uid!!) {
                selectedMessage = it
                chatsDialog.show()
            }
            binding.chatsRecyclerView.adapter = chatAdapter
        }

        viewModel.getLatestChats(profileId!!, isGroup)

    }

    fun registerClickListeners() {
        giphy.gifSelectionListener = object : GiphyDialogFragment.GifSelectionListener{
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
                    viewModel.sendFileMessage(
                        null,
                        gifUrl,
                        MessageType.GIF,
                        profileId!!,
                        isGroup,
                        profileGroup?.groupMembers?.keys?.toList() ?: emptyList(),
                        profileGroup,
                        profile
                    )
                }
            }

        }
        binding.apply {
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }
            sendMessage.setOnClickListener {
                if (messageInputET.text.toString().isNotEmpty()) {
                    viewModel.sendMessage(
                        profileId!!, MessageType.TEXT,
                        messageInputET.text.toString().trim(), "",
                        isGroup,
                        profileGroup?.groupMembers?.keys?.toList() ?: emptyList(),
                        profileGroup,
                        profile
                    )
                    messageInputET.text.clear()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Message field is Empty.\nPlease add some message.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            videoCall.setOnClickListener {
                makeCall(true)
                progressDialog.show()
            }
            voiceCall.setOnClickListener {
                makeCall(false)
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
                                requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                            vibratorManager.defaultVibrator
                        } else {
                            requireActivity().getSystemService(VIBRATOR_SERVICE) as Vibrator
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
                giphy.show(parentFragmentManager, "giphy_dialog")
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
            profileName.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("searchUser", profile)
                findNavController().navigate(
                    R.id.action_chatsFragment_to_profileFragment,
                    args = bundle
                )
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

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun bindObserver() {
        viewModel.sendMessageStatus.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {}
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Some Error Occurred", Toast.LENGTH_SHORT)
                        .show()
                }

                is NetworkResult.Loading -> {

                }
            }
        }
        viewModel.chatList.observe(viewLifecycleOwner) { l ->
            val list = ArrayList<ChatEntity>()
            l.forEach {
                if (it.chatId.equals(chatId))
                    list.add(it)
            }
            if (isGroup) {
                groupChatsAdapter.submitList(list)
                binding.chatsRecyclerView.scrollToPosition(groupChatsAdapter.itemCount - 1)
            } else {
                chatAdapter.submitList(list)
                binding.chatsRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }
        viewModel.callSuccess.observe(viewLifecycleOwner) {
            it.onSuccess { c ->
                progressDialog.dismiss()
                startActivity(
                    Intent(
                        requireContext(),
                        if (c.isGroupCall) GroupCallActivity::class.java else CallActivity::class.java
                    )
                        .putExtra("agoraToken", token)
                        .putExtra("profileId", profileId)
                        .putExtra("byMe", true)
                        .putExtra("callId", callId)
                        .putExtra("typeVideo", c.typeVideo)
                        .putExtra("profileImage", c.receiverProfileImage)
                        .putExtra("profileName", c.receiverName)
                )
            }
            it.onFailure {
                Toast.makeText(
                    requireContext(),
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
                Toast.makeText(requireContext(), audioPath, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
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
            MediaRecorder(requireContext())
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
            requireActivity(),
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
                intent.data = Uri.parse(
                    String.format(
                        "package:%s",
                        requireActivity().applicationContext.packageName
                    )
                )
                startActivityForResult(intent, 2296)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(WRITE_EXTERNAL_STORAGE),
                332
            )
        }
    }

    private fun checkPermissionFromDevice(): Boolean {
        val write_external_storage_result =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        val record_audio_result =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
        return write_external_storage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED
    }

    private fun ActivityResult?.sendFiles() {
        this?.let {
            if (this@sendFiles.data?.clipData != null) {
                this@sendFiles.data?.clipData?.let { data ->
                    val list = ArrayList<Uri>()
                    for (i in 0 until data.itemCount) {
                        list.add(data.getItemAt(i).uri)
                    }
                    viewModel.sendMultipleFileMessage(
                        list,
                        profileId!!,
                        isGroup,
                        profileGroup?.groupMembers?.keys?.toList() ?: emptyList(),
                        profileGroup,
                        profile
                    )
                }
            } else if (this@sendFiles.data?.data != null) {
                viewModel.sendFileMessage(
                    this@sendFiles.data?.data,
                    "",
                    null,
                    profileId!!,
                    isGroup,
                    profileGroup?.groupMembers?.keys?.toList() ?: emptyList(),
                    profileGroup,
                    profile
                )
            } else {
                //do nothing
            }
        }
    }

    fun getChatId() = if (profile != null) {
        firebaseAuth.currentUser?.uid?.let {
            if (it.compareTo(profileId!!) > 0)
                it + profileId
            else
                profileId + it
        }
    } else {
        profileId
    }

}