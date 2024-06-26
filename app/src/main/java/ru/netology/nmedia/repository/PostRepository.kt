package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {

    val data: Flow<List<Post>>
    val dataWithHidden: Flow<List<Post>>

    suspend fun getAll()
    suspend fun getById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun retrySave(post: Post)
    fun getNewerCount(newerId: Long): Flow<Int>
    suspend fun getHidden()
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun retrySaveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun updateUser(login: String, pass: String)
    suspend fun registerUser(login: String, pass: String, name: String)
    suspend fun registerWithPhoto(login: String, pass: String, name: String, upload: MediaUpload)
}