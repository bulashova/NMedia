package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {
    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        ApiService.service
            .getAll()
            .enqueue(
                object : retrofit2.Callback<List<Post>> {
                    override fun onResponse(
                        call: Call<List<Post>>,
                        response: Response<List<Post>>
                    ) {
                        handlerResponse(response, callback)
                    }

                    override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                        callback.onError(java.lang.Exception(t))
                    }

                })
    }

    override fun getByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        ApiService.service
            .getById(id)
            .enqueue(
                object : retrofit2.Callback<Post> {
                    override fun onResponse(
                        call: Call<Post>,
                        response: Response<Post>
                    ) {
                        handlerResponse(response, callback)
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(java.lang.Exception(t))
                    }

                })
    }

    override fun likeByIdAsync(
        id: Long,
        likedByMe: Boolean,
        callback: PostRepository.Callback<Post>
    ) {
        ApiService.service
            .run { if (!likedByMe) likeByMe(id) else unLikeByMe(id) }
            .enqueue(
                object : retrofit2.Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        handlerResponse(response, callback)
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(java.lang.Exception(t))
                    }
                }
            )
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        ApiService.service.removeById(id).enqueue(
            object : retrofit2.Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>
                ) {
                    handlerResponse(response, callback)
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(java.lang.Exception(t))
                }
            })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        ApiService.service.save(post).enqueue(
            object : retrofit2.Callback<Post> {
                override fun onResponse(
                    call: Call<Post>,
                    response: Response<Post>
                ) {
                    handlerResponse(response, callback)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(java.lang.Exception(t))
                }
            }
        )
    }

    private fun <T> handlerResponse(
        response: Response<T>,
        callback: PostRepository.Callback<T>
    ) {
        if (!response.isSuccessful) {
            callback.onError(RuntimeException(response.message()))
            return
        }

        val bodyNotNull = response.body() ?: run {
            callback.onError(RuntimeException("body is null"))
            return
        }
        callback.onSuccess(bodyNotNull)
    }
}