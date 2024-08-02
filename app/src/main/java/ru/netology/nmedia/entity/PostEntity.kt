package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int? = 0,
    val views: Int? = 0,
    val videoURL: String? = "",
    val videoTitle: String? = "",
    val authorAvatar: String,
    var savedOnTheServer: Int = 1,
    val visibile: Boolean = true,
    @Embedded
    var attachment: AttachmentEmbeddable?
    ) {
    fun toDto() = Post(
        id,
        author,
        authorId,
        published,
        content,
        likedByMe,
        likes,
        shares,
        views,
        videoURL,
        videoTitle,
        authorAvatar,
        attachment?.toDto(),
        savedOnTheServer
    )

    companion object {
        fun fromDto(dto: Post, visibile: Boolean = true) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorId,
                dto.content,
                dto.published,
                dto.likes,
                dto.likedByMe,
                dto.shares,
                dto.views,
                dto.videoURL,
                dto.videoTitle,
                dto.authorAvatar,
                dto.savedOnTheServer,
                visibile,
                AttachmentEmbeddable.fromDto(dto.attachment)
            )
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType
) {
    fun toDto() = Attachment(
        url,
        type
    )

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(
                it.url,
                it.type
            )
        }
    }
}


fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(visibile: Boolean = true): List<PostEntity> =
    map { PostEntity.fromDto(it, visibile) }