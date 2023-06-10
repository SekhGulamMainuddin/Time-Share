package com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.GroupParticipantsItemBinding

class SelectParticipantsAdapter(
    val list: MutableList<Pair<User, Boolean>>,
    val context: Context, var participant: (Pair<Pair<User, Boolean>, Int>) -> Unit
) : RecyclerView.Adapter<SelectParticipantsAdapter.ParticipantsViewHolder>() {

    fun update(position: Int){
        list[position]= Pair(list[position].first, !list[position].second)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantsViewHolder {
        return ParticipantsViewHolder(
            GroupParticipantsItemBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            ), context
        )
    }

    override fun onBindViewHolder(holder: ParticipantsViewHolder, position: Int) {
        val item= list[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            participant.invoke(Pair(item, position))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ParticipantsViewHolder(
        val binding: GroupParticipantsItemBinding,
        val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<User,Boolean>) {
            binding.apply {
                Glide.with(context).load(item.first.imageUrl).placeholder(R.drawable.default_profile_pic).into(profileIcon)
                profileNamme.text= item.first.name
                profileDesc.text= item.first.bio
                checkedButton.isVisible= item.second
            }
        }

    }

}