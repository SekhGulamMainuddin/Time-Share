package com.sekhgmainuddin.timeshare.ui.home.notification

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sekhgmainuddin.timeshare.databinding.ActivityNotificationsBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.notification.adapters.NotificationsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity(), NotificationsAdapter.OnClickNotify {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        notificationsAdapter = NotificationsAdapter(this, this)
        binding.notificationsRecyclerView.adapter= notificationsAdapter

        viewModel.getNotifications()

        viewModel.notifications.observe(this) {
            it.onSuccess { notifications ->
                notificationsAdapter.submitList(notifications.notifications)
            }
            it.onFailure {
                Toast.makeText(this, "Some Failure Occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun friendRequestAccepted(id: String) {
        viewModel.verifyAndAcceptRequest(id)
        Toast.makeText(this, "Request Accepted", Toast.LENGTH_SHORT).show()
    }


}