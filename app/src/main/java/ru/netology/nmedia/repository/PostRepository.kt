package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {

    fun getAllAsync(callback: Callback<List<Post>>)
    fun findByIdAsync(id: Long, callback: Callback<Post>)
    fun likeByIdAsync(id: Long, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun saveAsync(post: Post, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(exception: Exception)
    }
}