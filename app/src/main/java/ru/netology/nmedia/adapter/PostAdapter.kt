package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.Count
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.SeparatorItem
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
    fun onPreviewAttachment(post: Post)
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post, null -> R.layout.card_post
            is SeparatorItem -> R.layout.item_separator
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val view =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(view, onInteractionListener)
            }

            R.layout.card_ad -> {
                val view = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(view)
            }

            R.layout.item_separator -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_separator, parent, false)
                SeparatorViewHolder(view)
            }

            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is SeparatorItem -> (holder as? SeparatorViewHolder)?.bind(item.text)
            null -> Unit
        }
    }
}

class SeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val description: TextView = view.findViewById(R.id.separator_description)
    fun bind(separatorText: String) {
        description.text = separatorText
    }

    companion object {
        fun create(parent: ViewGroup): SeparatorViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_separator, parent, false)
            return SeparatorViewHolder(view)
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.image.load("${BuildConfig.BASE_URL}media/${ad.image}")
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
            published.text = post.published.toString()
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
                attachment.load("${BASE_URL}media/${post.attachment.url}")
                attachment.setOnClickListener {
                    onInteractionListener.onPreviewAttachment(post)
                }
            } else attachment.visibility = View.GONE

            like.isChecked = post.likedByMe
            like.text = Count.formatCount(post.likes)
            share.text = post.shares?.let { Count.formatCount(it) }
            view.text = post.views?.let { Count.formatCount(it) }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
                like.isChecked = post.likedByMe
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.isVisible = post.ownedByMe && post.savedOnTheServer == 1

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

object PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}