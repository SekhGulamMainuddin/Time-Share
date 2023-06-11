package com.sekhgmainuddin.timeshare.ui.home.home.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.StatusHolderRvBinding
import com.sekhgmainuddin.timeshare.ui.home.home.HomeScreenFragment
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo

class PostsAdapter(val context: Context, val onClick: PostsAdapter.OnClick) :
    ListAdapter<PostEntity, RecyclerView.ViewHolder>(PostDiffCallBack()), StatusAdapter.OnClick {

    val statusList = ArrayList<Pair<List<Status>, User>>()
    fun updateStatus(list: List<Pair<List<Status>, User>>) {
        statusList.clear()
        statusList.addAll(list)
        Log.d("statusUpdate", "updateStatus: ${statusList.size}")
        notifyItemChanged(0)
    }

    private class PostDiffCallBack : DiffUtil.ItemCallback<PostEntity>() {
        override fun areItemsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean {
            return oldItem.creatorId == newItem.creatorId
        }

        override fun areContentsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean {
            return oldItem == newItem
        }

    }

    class PostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postDescription = itemView.findViewById<TextView>(R.id.postDescription)
        val likeCount = itemView.findViewById<TextView>(R.id.likeCount)
        val commentCount = itemView.findViewById<TextView>(R.id.commentCount)
        val viewPager = itemView.findViewById<ViewPager2>(R.id.viewPagerImageVideo)
        val tabLayout = itemView.findViewById<TabLayout>(R.id.tabLayout)
        val likeIcon = itemView.findViewById<ImageButton>(R.id.likeButton)
        val commentIcon = itemView.findViewById<ImageButton>(R.id.commentButton)
        val creatorName = itemView.findViewById<TextView>(R.id.profileName)
        val postDate = itemView.findViewById<TextView>(R.id.postDate)
        val creatorProfileImage = itemView.findViewById<ShapeableImageView>(R.id.profileImage)
        val shareButton = itemView.findViewById<ImageButton>(R.id.shareButton)
        val savePostButton = itemView.findViewById<ImageButton>(R.id.savePostButton)
        val likeAnimation = itemView.findViewById<LottieAnimationView>(R.id.likeAnimation)
        val commenttext = itemView.findViewById<TextView>(R.id.addCommentText)
    }

    class StatusViewHolder(val binding: StatusHolderRvBinding, val context: Context, val onClick: StatusAdapter.OnClick) :
        RecyclerView.ViewHolder(binding.root) {

        private var adapter: StatusAdapter? = null

        fun loadViews(list: List<Pair<List<Status>, User>>){
            if (adapter==null){
                adapter= StatusAdapter(context, onClick)
                binding.statusRecyclerView.adapter= adapter
            }
            update(list)
        }

        fun update(list: List<Pair<List<Status>, User>>){
            adapter?.submitList(list)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType != 0)
            PostsViewHolder(
                LayoutInflater.from(context).inflate(R.layout.posts_layout_rv, parent, false)
            )
        else
            StatusViewHolder(
                StatusHolderRvBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                ), context,
                this
            )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            val holder = viewHolder as StatusViewHolder
            holder.loadViews(statusList)
        } else {
            val item = currentList[position]
            val holder = viewHolder as PostsViewHolder
            val viewPagerAdapter = ImageVideoViewPagerAdapter(context, item) {
                if (it == 1)
                    onClick.postClicked(item)
                else if (it == 2) {
                    onClick.likePost(item.postId)
                    playLikeAnimation(holder)
                }
            }
            holder.viewPager.adapter = viewPagerAdapter
            TabLayoutMediator(holder.tabLayout, holder.viewPager)
            { _, _ -> }.attach()
            if ((item.postContent?.size ?: 0) > 1)
                holder.tabLayout.visibility = View.INVISIBLE
            else
                holder.tabLayout.visibility = View.VISIBLE
            holder.creatorName.text = item.creatorName
            holder.postDate.text = item.postTime.getTimeAgo()
            Glide.with(context).load(item.creatorProfileImage)
                .placeholder(R.drawable.default_profile_pic).into(holder.creatorProfileImage)
            holder.postDescription.text = item.postDesc
            holder.likeCount.text = item.likesCount.toString()
            holder.commentCount.text = item.commentCount.toString()
            if (item.myComment.isEmpty()) {
                holder.commenttext.text = context.getString(R.string.add_a_comment)
                holder.commenttext.setTextColor(context.getColor(R.color.profile_text_color))
            } else {
                holder.commenttext.text = item.myComment
                holder.commenttext.setTextColor(context.getColor(R.color.semi_black))
            }
            holder.likeIcon.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    if (item.likedAndCommentByMe in intArrayOf(
                            1,
                            3
                        )
                    ) R.drawable.liked_icon else R.drawable.love_icon
                )
            )
            holder.commentIcon.imageTintList = ColorStateList.valueOf(
                context.getColor(
                    if (item.likedAndCommentByMe in intArrayOf(
                            2,
                            3
                        )
                    ) R.color.orangePink else R.color.black
                )
            )
            holder.savePostButton.setOnClickListener {
                onClick.savePost(item)
            }
            holder.shareButton.setOnClickListener {
                onClick.sharePost(item)
            }
            holder.likeIcon.setOnClickListener {
                playLikeAnimation(holder)
            }
            holder.commenttext.setOnClickListener {
                onClick.postClicked(item)
            }
        }
    }

    private fun playLikeAnimation(holder: PostsViewHolder) {
        holder.likeAnimation?.visibility = View.VISIBLE
        holder.likeAnimation?.playAnimation()
        Handler(Looper.getMainLooper()).postDelayed({
            holder.likeAnimation?.cancelAnimation()
            holder.likeAnimation?.visibility = View.GONE
        }, 750)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }


    interface OnClick {
        fun postClicked(post: PostEntity)
        fun savePost(post: PostEntity)
        fun sharePost(post: PostEntity)
        fun likePost(postId: String)
        fun showStatus(position: Int)
    }

    override fun statusClicked(position: Int) {
        onClick.showStatus(position)
    }
}