package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.insertSeparators
import androidx.paging.map
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
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.SeparatorItem
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) :
    PostRepository {

    @Inject
    lateinit var appAuth: AppAuth

    @RequiresApi(Build.VERSION_CODES.O)
    private val currentDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val yesterday = currentDate.minusDays(1)

    @RequiresApi(Build.VERSION_CODES.O)
    private val weekAgo = currentDate.minusDays(7)

    @RequiresApi(Build.VERSION_CODES.O)
    private val twoWeekAgo = currentDate.minusDays(14)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateOfPublication(published: Long) =
        Instant.ofEpochSecond(published)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    @OptIn(ExperimentalPagingApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override val data = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )

    ).flow
        .map {
            it.map(PostEntity::toDto)
                .insertSeparators { previos, _ ->
                    if (previos?.id?.rem(5) == 0L) {
                        Ad(Random.nextLong(), previos.published, "figma.jpg")
                    } else {
                        null
                    }
                }
                .insertSeparators { before, after ->
                    when {
                        before == null -> null
                        after == null -> null
                        (dateOfPublication(before.published) != currentDate &&
                                dateOfPublication(after.published) == currentDate) ->
                            SeparatorItem(
                                Random.nextLong(),
                                0,
                                "Today",
                            )

                        (dateOfPublication(before.published) != yesterday &&
                                dateOfPublication(after.published) == yesterday) ->
                            SeparatorItem(
                                Random.nextLong(),
                                0,
                                "Yesterday"
                            )

                        (dateOfPublication(before.published) != weekAgo &&
                                dateOfPublication(after.published) == weekAgo) ->
                            SeparatorItem(
                                Random.nextLong(),
                                0,
                                "Week ago"
                            )

                        (dateOfPublication(before.published) != twoWeekAgo &&
                                dateOfPublication(after.published) == twoWeekAgo) ->
                            SeparatorItem(
                                Random.nextLong(),
                                0,
                                "Two week ago"
                            )

                        else -> null
                    }
                }
        }

    override val dataWithHidden: Flow<List<Post>> = dao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        if (dao.isEmpty()) {
            try {
                val response = apiService.getAll()
                if (!response.isSuccessful) throw ApiError(
                    response.code(),
                    response.message()
                )
                val posts =
                    response.body() ?: throw ApiError(response.code(), response.message())
                posts.map { it.savedOnTheServer = 1 }
                dao.insert(posts.toEntity())
                dao.showAll()
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

    override suspend fun getById(id: Long): LiveData<Post> {
        return requireNotNull(dao.getById(id)).map { it.toDto() }
//        try {
//            val response = apiService.getById(id)
//            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
//            val post = response.body() ?: throw ApiError(response.code(), response.message())
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        dao.likeById(id)
        try {
            val response = apiService
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
        post.authorId = appAuth.authStateFlow.value.id
        post.savedOnTheServer = 1
        dao.insert(PostEntity.fromDto(post))
//        try {
//            val response = apiService.save(post)
//            if (!response.isSuccessful) {
//                post.savedOnTheServer = 0
//                dao.insert(PostEntity.fromDto(post.copy(id = cacheId)))
//                cacheId++
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            body.savedOnTheServer = 1
//            dao.insert(PostEntity.fromDto(body))
//        } catch (e: IOException) {
//            post.savedOnTheServer = 0
//            dao.insert(PostEntity.fromDto(post.copy(id = cacheId)))
//            cacheId++
//            throw NetworkError
//        } catch (e: Exception) {
//            post.savedOnTheServer = 0
//            dao.insert(PostEntity.fromDto(post.copy(id = cacheId)))
//            cacheId++
//            throw UnknownError
//        }
    }

    override suspend fun retrySave(post: Post) {
        try {
            val response = apiService.save(post.copy(id = 0L))
            if (!response.isSuccessful) {
                post.savedOnTheServer = 0
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
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
//        try {
//            val response = apiService.removeById(id)
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
    }

    override fun getNewerCount(newerId: Long): Flow<Long> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewerCount(newerId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            emit(body.count)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

//    override fun getNewer(newerId: Long): Flow<Int> = flow {
//        while (true) {
//            delay(10_000L)
//            val response = apiService.getNewer(newerId)
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            body.map { it.savedOnTheServer = 1 }
//            dao.insert(body.toEntity(false))
//            emit(dao.countHidden())
//        }
//    }
//        .catch { e -> throw AppError.from(e) }
//        .flowOn(Dispatchers.Default)

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
            val response = apiService.upload(part)
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

    override suspend fun updateUser(login: String, pass: String) {
        try {
            val response = apiService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            println(response.body())
            response.body() ?: throw ApiError(response.code(), response.message())
            response.body()?.let {
                appAuth.setAuth(it.id, it.token)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(login: String, pass: String, name: String) {
        try {
            val response = apiService.registerUser(login, pass, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            println(response.body())
            response.body() ?: throw ApiError(response.code(), response.message())
            response.body()?.let {
                appAuth.setAuth(it.id, it.token)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerWithPhoto(
        login: String,
        pass: String,
        name: String,
        upload: MediaUpload
    ) {
        try {
            val part = MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
            )

            val response = apiService.registerWithPhoto(
                login.toRequestBody(),
                pass.toRequestBody(),
                name.toRequestBody(),
                part
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            println(response.body())
            response.body() ?: throw ApiError(response.code(), response.message())
            response.body()?.let {
                appAuth.setAuthWithPhoto(it.id, it.token, it.avatar)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}