package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {

    val data: LiveData<List<Post>>

    suspend fun getAll()
    suspend fun getById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun retrySave(post: Post)
}