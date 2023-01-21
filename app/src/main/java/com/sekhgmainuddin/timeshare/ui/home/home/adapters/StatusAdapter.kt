package com.sekhgmainuddin.timeshare.ui.home.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.Status

class StatusAdapter(val context: Context): ListAdapter<Status, StatusAdapter.StatusViewHolder>(StatusDiffCallBack()) {

    private class StatusDiffCallBack() : DiffUtil.ItemCallback<Status>(){
        override fun areItemsTheSame(oldItem: Status, newItem: Status): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: Status, newItem: Status): Boolean {
            return oldItem==newItem
        }

    }


    class StatusViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val text= itemView.findViewById<TextView>(R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        return StatusViewHolder(LayoutInflater.from(context).inflate(R.layout.status_layout_rv, parent, false))
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.text.text= position.toString()
    }

}