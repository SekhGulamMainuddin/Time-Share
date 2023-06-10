package com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.FragmentAddGroupBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.adapters.CurrentSelectedParticipantsAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.adapters.SelectParticipantsAdapter
import com.sekhgmainuddin.timeshare.utils.Utils.slideVisibility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddGroupFragment : Fragment() {

    private var _binding: FragmentAddGroupBinding? = null
    private val binding: FragmentAddGroupBinding
        get() = _binding!!
    private var currentGroupState = 0
    private var groupNameValue: String = ""
    private var groupDescValue: String = ""
    private var groupImageUri: Uri? = null
    private val selectedList = ArrayList<Pair<User, Int>>()
    private lateinit var currentSelectedParticipantsAdapter: CurrentSelectedParticipantsAdapter
    private lateinit var selectParticipantsAdapter: SelectParticipantsAdapter
    private val viewModel by activityViewModels<ChatsViewModel>()
    private lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGroupBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.addGroupContainer.isVisible = false
        binding.guideline40.setGuidelinePercent(1f)

        showHideStatus0(true)
        registerListeners()
        loadData()
        bindObservers()

    }

    private fun loadData() {
        val userList = viewModel.friendsList.values.map { Pair(it, false) }.toMutableList()
        currentSelectedParticipantsAdapter = CurrentSelectedParticipantsAdapter(requireContext()) {
            selectParticipantsAdapter.update(it.second)
            val newSelectedList = ArrayList<Pair<User, Int>>()
            selectedList.forEach { p ->
                if (p.second != it.second)
                    newSelectedList.add(p)
            }
            selectedList.clear()
            selectedList.addAll(newSelectedList)
            currentSelectedParticipantsAdapter.update(selectedList)
        }
        selectParticipantsAdapter = SelectParticipantsAdapter(userList, requireContext()) {
            if (!it.first.second) {
                selectedList.add(Pair(it.first.first, it.second))
            } else {
                val newSelectedList = ArrayList<Pair<User, Int>>()
                selectedList.forEach { p ->
                    if (p.second != it.second)
                        newSelectedList.add(p)
                }
                selectedList.clear()
                selectedList.addAll(newSelectedList)
            }
            currentSelectedParticipantsAdapter.update(selectedList)
            selectParticipantsAdapter.update(it.second)
        }

        binding.currentSelectedParticipants.adapter = currentSelectedParticipantsAdapter
        binding.selectParticipantsRecyclerView.adapter = selectParticipantsAdapter
        val selectedLayoutManager = GridLayoutManager(requireContext(), 4)
        binding.selectedParticipantsRecyclerView.layoutManager = selectedLayoutManager
        binding.selectedParticipantsRecyclerView.adapter = currentSelectedParticipantsAdapter

    }

    private fun registerListeners() {
        binding.apply {
            nextButton.setOnClickListener {
                if (currentGroupState == 0) {
                    if (groupNameValue.isEmpty()){
                        groupNameEditText.error= "Enter Group Name"
                        Toast.makeText(requireContext(), "Enter Group Name", Toast.LENGTH_SHORT).show()
                    }
                    else if (groupDescValue.isEmpty()){
                        groupDescEditText.error= "Enter Group Description"
                        Toast.makeText(requireContext(), "Enter Group Description", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        binding.guideline40.setGuidelinePercent(0.4f)
                        showHideStatus0()
                        currentGroupState = 1
                    }
                } else if (currentGroupState == 1) {
                    if (selectedList.isEmpty()){
                        Toast.makeText(requireContext(), "Select Participants", Toast.LENGTH_SHORT).show()
                    }else{
                        showHideStatus01(false)
                        nextButton.setImageResource(R.drawable.single_tick)
                        selectedParticipantsRecyclerView.slideVisibility(true)
                        currentGroupState = 2
                    }
                } else {
                    progressDialog.show()
                    viewModel.createGroup(
                        groupNameValue,
                        groupDescValue,
                        groupImageUri,
                        selectedList.map { p ->
                            p.first.userId.trim()
                        }.toMutableList()
                    )
                }
            }
            toolBar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            groupNameEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    groupNameValue = s.toString()
                    groupName.text = groupNameValue
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            clearGroupName.setOnClickListener { groupNameEditText.setText("") }
            groupDescEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    groupDescValue = s.toString()
                    groupDesc.text = groupDescValue
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            clearGroupDesc.setOnClickListener { groupDescEditText.setText("") }
            editGroupIcon.setOnClickListener {
                imageIntent.launch("image/*")
            }
        }
    }

    val imageIntent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        groupImageUri = it
        Glide.with(requireContext()).load(it).into(binding.groupIcon)
        Glide.with(requireContext()).load(it).into(binding.groupIconSet)
    }

    private fun showHideStatus0(show: Boolean = false) {
        binding.apply {
            groupNameEditText.slideVisibility(show)
            groupDescEditText.slideVisibility(show)
            clearGroupName.slideVisibility(show)
            clearGroupDesc.slideVisibility(show)
            groupIconSet.slideVisibility(show)
            editGroupIcon.slideVisibility(show)
            showHideStatus01(!show)
        }
    }

    private fun showHideStatus01(status: Boolean) {
        binding.apply {
            addGroupContainer.slideVisibility(status || currentGroupState!=0)
            selectParticipantsRecyclerView.slideVisibility(status)
            currentSelectedParticipants.slideVisibility(status)
            groupIcon.slideVisibility(status || currentGroupState!=0)
            groupDesc.slideVisibility(status || currentGroupState!=0)
            groupName.slideVisibility(status || currentGroupState!=0)
        }
    }

    private fun bindObservers() {
        viewModel.resultGroupStatus.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
            it.onSuccess {
                Toast.makeText(requireContext(), "Group Created", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            it.onFailure {
                Toast.makeText(requireContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        _binding = null
    }


}