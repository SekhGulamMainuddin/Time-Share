package com.sekhgmainuddin.timeshare.ui.home.home.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.adapters.ImageVideoViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.adapters.onClick
import com.sekhgmainuddin.timeshare.ui.home.home.HomeScreenFragment

class PostsAdapter(val context: Context, val onClick: HomeScreenFragment): ListAdapter<PostEntity,PostsAdapter.PostsViewHolder>(PostDiffCallBack()),
    onClick {

    private class PostDiffCallBack: DiffUtil.ItemCallback<PostEntity>() {
        override fun areItemsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean {
            return oldItem.creatorId == newItem.creatorId
        }

    }

    class PostsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val postDescription= itemView.findViewById<TextView>(R.id.postDescription)
        val likeCount= itemView.findViewById<TextView>(R.id.likeCount)
        val commentCount= itemView.findViewById<TextView>(R.id.commentCount)
        val viewPager= itemView.findViewById<ViewPager2>(R.id.viewPagerImageVideo)
        val tabLayout= itemView.findViewById<TabLayout>(R.id.tabLayout)
        val likeIcon= itemView.findViewById<ImageButton>(R.id.likeButton)
        val commentIcon= itemView.findViewById<ImageButton>(R.id.commentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(LayoutInflater.from(context).inflate(R.layout.posts_layout_rv, parent, false))
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val item= currentList[position]
        val viewPagerAdapter = ImageVideoViewPagerAdapter(context,this)
        holder.viewPager.adapter = viewPagerAdapter
        item.postContent?.let { viewPagerAdapter.update(it) }
        TabLayoutMediator(holder.tabLayout, holder.viewPager)
        {_,_ -> }.attach()
        holder.postDescription.text= item.postDesc
        holder.likeCount.text= item.likesCount.toString()
        holder.commentCount.text= item.commentCount.toString()
        holder.likeIcon.setImageDrawable(AppCompatResources.getDrawable(context,if (item.likedAndCommentByMe in intArrayOf(1,3)) R.drawable.liked_icon else R.drawable.love_icon))
        holder.commentIcon.imageTintList= ColorStateList.valueOf(context.getColor(if (item.likedAndCommentByMe in intArrayOf(2,3)) R.color.orangePink else R.color.black))
        holder.itemView.setOnClickListener {
            onClick.postClicked(item)
        }
    }

    override fun onNewAddClick() {}

    override fun onClickToView(postImageVideo: PostImageVideo) {}

    interface OnClick{
        fun postClicked(post: PostEntity)
    }

}