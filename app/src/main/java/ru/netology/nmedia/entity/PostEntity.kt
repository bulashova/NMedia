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
    val authorAvatar: String
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
        authorAvatar = authorAvatar
    )

    companion object {
        fun fromDto(dto: Post): PostEntity = with(dto) {
            PostEntity(
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
                authorAvatar = authorAvatar
            )
        }
    }
}