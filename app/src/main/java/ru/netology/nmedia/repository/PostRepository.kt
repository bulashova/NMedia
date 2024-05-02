package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {

    fun getAllAsync(callback: Callback<List<Post>>)
    fun getByIdAsync(id: Long, callback: Callback<Post>)
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun saveAsync(post: Post, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(exception: Exception)
    }
}