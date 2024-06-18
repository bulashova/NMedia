package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val authorId: Long,
    val published: String,
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
)

data class Attachment(
    val url: String,
    val type: AttachmentType = AttachmentType.IMAGE
)