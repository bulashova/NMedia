package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryImpl(private val dao: PostDao) :
    PostRepository {

    override val data: Flow<List<Post>> = dao.getAllVisible()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override val dataWithHidden: Flow<List<Post>> = dao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = ApiService.retrofitService.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            posts.map { it.savedOnTheServer = 1 }
            posts.map{ it.likedByMe = it.likes != 0}
            dao.insert(posts.toEntity())
            dao.showAll()
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

    override fun getNewerCount(newerId: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = ApiService.retrofitService.getNewer(newerId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.map { it.savedOnTheServer = 1 }
            dao.insert(body.toEntity(false))
            emit(dao.countHidden())
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun getHidden() {
        try {
            dao.showAll()
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        save(
            post.copy(
                attachment = Attachment(
                    upload(upload).id,
                    AttachmentType.IMAGE
                )
            )
        )
    }

    override suspend fun retrySaveWithAttachment(post: Post, upload: MediaUpload) {
        retrySave(
            post.copy(
                attachment = Attachment(
                    upload(upload).id,
                    AttachmentType.IMAGE
                )
            )
        )
    }

    suspend fun upload(upload: MediaUpload): Media {
        try {
            val part = MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
            )
            val response = ApiService.retrofitService.upload(part)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun updateUser(login: String, pass: String){
        try {
            val response = ApiService.retrofitService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            println(response.body())
            response.body() ?: throw ApiError(response.code(), response.message())
            response.body()?.let {
                AppAuth.getInstance().setAuth(it.id, it.token)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(login: String, pass: String, name: String){
        try {
            val response = ApiService.retrofitService.registerUser(login, pass, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            println(response.body())
            response.body() ?: throw ApiError(response.code(), response.message())
            response.body()?.let {
                AppAuth.getInstance().setAuth(it.id, it.token)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

   override suspend fun registerWithPhoto(login: String, pass: String, name: String, upload: MediaUpload){
       try {
           val part = MultipartBody.Part.createFormData(
               "file",
               upload.file.name,
               upload.file.asRequestBody()
           )

           val response = ApiService.retrofitService.registerWithPhoto(
               login.toRequestBody(),
               pass.toRequestBody(),
               name.toRequestBody(),
               part)
           if (!response.isSuccessful) {
               throw ApiError(response.code(), response.message())
           }
           println(response.body())
           response.body() ?: throw ApiError(response.code(), response.message())
           response.body()?.let {
               AppAuth.getInstance().setAuthWithPhoto(it.id, it.token, it.avatar)
           }
       } catch (e: IOException) {
           throw NetworkError
       } catch (e: Exception) {
           throw UnknownError
       }
   }
}