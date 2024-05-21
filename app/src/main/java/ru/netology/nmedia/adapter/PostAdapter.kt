package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.Count
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.glide.load
import ru.netology.nmedia.glide.loadCircleCrop

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onCancel(post: Post)
    fun onPlayVideo(post: Post)
    fun onPreview(post: Post)
    fun onRetrySave(post: Post)
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(view, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999/"
    }

    fun bind(post: Post) {
        with(binding) {
            avatar.loadCircleCrop("${BASE_URL}avatars/${post.authorAvatar}")
            author.text = post.author
            published.text = post.published
            content.text = post.content

            if (post.savedOnTheServer == 1) {
                actionGroup.visibility = View.VISIBLE
                unsaved.visibility = View.GONE
            } else {
                actionGroup.visibility = View.GONE
                unsaved.visibility = View.VISIBLE
                unsaved.setOnClickListener {
                    onInteractionListener.onRetrySave(post)
                }
            }

            if (!post.videoURL.isNullOrBlank()) {
                group.visibility = View.VISIBLE
                video.setImageResource(R.drawable.ic_youtube)
                videoTitle.text = post.videoTitle
                play.setOnClickListener {
                    onInteractionListener.onPlayVideo(post)
                }
                video.setOnClickListener {
                    onInteractionListener.onPlayVideo(post)
                }
                videoTitle.setOnClickListener {
                    onInteractionListener.onPlayVideo(post)
                }
            } else group.visibility = View.GONE

            if (post.attachment != null) {
                attachment.visibility = View.VISIBLE
                attachment.load("${BASE_URL}images/${post.attachment.url}")
            } else attachment.visibility = View.GONE

            like.isChecked = post.likedByMe
            like.text = Count.formatCount(post.likes)
            share.text = post.shares?.let { Count.formatCount(it) }
            view.text = post.views?.let { Count.formatCount(it) }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            if (post.savedOnTheServer == 1) {
                root.setOnClickListener {
                    onInteractionListener.onPreview(post)
                }
            }
        }
    }
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {

    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem

}