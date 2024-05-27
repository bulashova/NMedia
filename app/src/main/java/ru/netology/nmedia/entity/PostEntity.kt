package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int? = 0,
    val views: Int? = 0,
    val videoURL: String? = "",
    val videoTitle: String? = "",
    val authorAvatar: String,
    var savedOnTheServer: Int = 1,
    val visibile: Boolean = true
) {
    fun toDto(): Post = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likedByMe = likedByMe,
        likes = likes,
        shares = shares,
        views = views,
        videoURL = videoURL,
        videoTitle = videoTitle,
        authorAvatar = authorAvatar,
        savedOnTheServer = savedOnTheServer
    )

    companion object {
        fun fromDto(dto: Post, visibile: Boolean = true) =
            PostEntity(
                dto.id,
                dto.author,
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
                visibile)
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(visibile: Boolean = true): List<PostEntity> = map { PostEntity.fromDto(it, visibile) }
