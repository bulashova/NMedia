package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed interface FeedItem {
    val id: Long
    val published: Long
}

data class Post(
    override val id: Long,
    val author: String,
    var authorId: Long,
    override val published: Long,
    var content: String,
    var likedByMe: Boolean = false,
    val likes: Int = 0,
    val shares: Int? = 0,
    val views: Int? = 0,
    val videoURL: String? = "",
    val videoTitle: String? = "",
    val authorAvatar: String,
    val attachment: Attachment? = null,
    var savedOnTheServer: Int = 0,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    override val published: Long = 0,
    val image: String,
) : FeedItem

data class SeparatorItem(
    override val id: Long,
    override val published: Long = 0,
    val text: String,
) : FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType = AttachmentType.IMAGE
)