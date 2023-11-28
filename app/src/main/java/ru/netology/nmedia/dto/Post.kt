package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val videoURL: String = "",
    val videoTitle: String = ""
)