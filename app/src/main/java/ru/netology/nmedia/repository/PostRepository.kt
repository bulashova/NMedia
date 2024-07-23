package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {

    val data: Flow<PagingData<FeedItem>>
    val dataWithHidden: Flow<List<Post>>

    suspend fun getAll()
    suspend fun getById(id: Long): LiveData<Post>
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun retrySave(post: Post)
    fun getNewerCount(newerId: Long): Flow<Long>
    suspend fun getHidden()
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun retrySaveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun updateUser(login: String, pass: String)
    suspend fun registerUser(login: String, pass: String, name: String)
    suspend fun registerWithPhoto(login: String, pass: String, name: String, upload: MediaUpload)
    // fun getNewer(newerId: Long): Flow<Int>
}