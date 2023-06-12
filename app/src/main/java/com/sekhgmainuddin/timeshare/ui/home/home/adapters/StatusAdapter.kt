package com.sekhgmainuddin.timeshare.ui.home.home.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.StatusLayoutRvBinding
import com.sekhgmainuddin.timeshare.utils.enums.StatusType

class StatusAdapter(val context: Context, val onClick: OnClick) :
    ListAdapter<Pair<List<Status>, User>, StatusAdapter.StatusViewHolder>(StatusDiffCallBack()) {

    private class StatusDiffCallBack() : DiffUtil.ItemCallback<Pair<List<Status>, User>>(){
        override fun areItemsTheSame(oldItem: Pair<List<Status>, User>, newItem: Pair<List<Status>, User>): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: Pair<List<Status>, User>, newItem: Pair<List<Status>, User>): Boolean {
            return oldItem.first==newItem.first && oldItem.second==newItem.second
        }

    }

    class StatusViewHolder(val binding: StatusLayoutRvBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<List<Status>, User>) {
            binding.apply {
                val status = item.first[0]
                val statusType = StatusType.valueOf(status.type)
                mainLayout.setCardBackgroundColor(ColorStateList.valueOf(status.background))
                iconButton.isVisible = status.statusUploadTime == 0L || statusType==StatusType.VIDEO
                if (status.statusUploadTime==0L || statusType==StatusType.VIDEO){
                    iconButton.setImageDrawable(AppCompatResources.getDrawable(context, if (status.statusUploadTime==0L) R.drawable.add_photo_video_icon else R.drawable.play_icon))
                }
                profileName.text = item.second.name
                Glide.with(context).load(item.second.imageUrl)
                    .placeholder(R.drawable.default_profile_pic).into(profileImage)
                if (status.statusUploadTime != 0L) {
                    statusText.isVisible = statusType == StatusType.TEXT
                    statusImage.isVisible = (statusType == StatusType.IMAGE || status.thumbnail.isNotEmpty())
                    if (status.thumbnail.isNotEmpty())
                        Glide.with(context).load(status.thumbnail).into(statusImage)
                    when (statusType) {
                        StatusType.IMAGE -> {
                            Glide.with(context).load(status.urlOrText).placeholder(R.color.black)
                                .into(statusImage)
                        }
                        StatusType.TEXT -> {
                            statusText.text = status.urlOrText
                        }
                        else -> {}
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        return StatusViewHolder(
            StatusLayoutRvBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            ), context
        )
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClick.statusClicked(position)
        }
    }

    interface OnClick{
        fun statusClicked(position: Int)
    }

}