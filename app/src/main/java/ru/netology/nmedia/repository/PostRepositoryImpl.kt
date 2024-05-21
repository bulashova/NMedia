package ru.netology.nmedia.repository

import androidx.lifecycle.map
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryImpl(private val dao: PostDao) :
    PostRepository {

    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = ApiService.retrofitService.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            posts.map { it.savedOnTheServer = 1 }
            dao.insert(posts.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long) {
        try {
            val response = ApiService.retrofitService.getById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val post = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        dao.likeById(id)
        try {
            val response = ApiService.retrofitService
                .run { if (!likedByMe) likeByMe(id) else unLikeByMe(id) }
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            dao.likeById(id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(id)
            throw UnknownError
        }
    }

    private var cacheId: Long = 10_000
    override suspend fun save(post: Post) {
        try {
            val response = ApiService.retrofitService.save(post)
            if (!response.isSuccessful) {
                post.savedOnTheServer = 0
                dao.insert(PostEntity.fromDto(post.copy(id = cacheId)))
                cacheId++
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.savedOnTheServer = 1
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            post.savedOnTheServer = 0
            dao.insert(PostEntity.fromDto(post.copy(id = cacheId)))
            cacheId++
            throw NetworkError
        } catch (e: Exception) {
            post.savedOnTheServer = 0
            dao.insert(PostEntity.fromDto(post.copy(id = cacheId)))
            cacheId++
            throw UnknownError
        }
    }

    override suspend fun retrySave(post: Post) {
        try {
            val response = ApiService.retrofitService.save(post.copy(id = 0L))
            if (!response.isSuccessful) {
                post.savedOnTheServer = 0
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.removeById(post.id)
            body.savedOnTheServer = 1
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            val response = ApiService.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}