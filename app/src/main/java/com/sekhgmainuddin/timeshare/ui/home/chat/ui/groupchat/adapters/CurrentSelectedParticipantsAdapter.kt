package com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.SelectedParticipantsItemBinding

class CurrentSelectedParticipantsAdapter(
    val context: Context, var participant: (Pair<User, Int>) -> Unit
) : RecyclerView.Adapter<CurrentSelectedParticipantsAdapter.CurrentParticipantsViewHolder>() {
    inner class CurrentParticipantsViewHolder(
        val binding: SelectedParticipantsItemBinding,
        val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        val removeUserIcon= binding.deleteParticipant

        fun bind(item: User) {
            binding.apply {
                Glide.with(context).load(item.imageUrl).placeholder(R.drawable.default_profile_pic).into(profileImage)
            }
        }

    }

    val list= ArrayList<Pair<User, Int>>()

    fun update(newList: ArrayList<Pair<User, Int>>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentParticipantsViewHolder {
        return CurrentParticipantsViewHolder(
            SelectedParticipantsItemBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            ), context
        )
    }

    override fun onBindViewHolder(holder: CurrentParticipantsViewHolder, position: Int) {
        val item= list[position]
        holder.bind(item.first)
        holder.removeUserIcon.setOnClickListener {
            participant.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}