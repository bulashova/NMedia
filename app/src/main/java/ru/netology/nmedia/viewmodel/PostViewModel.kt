package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    published = "",
    authorAvatar = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val edited = MutableLiveData(empty)

    private val _errorLoad = SingleLiveEvent<Exception?>()
    val errorLoad: SingleLiveEvent<Exception?>
        get() = _errorLoad

    private var _errorSave = SingleLiveEvent<Exception?>()
    val errorSave: SingleLiveEvent<Exception?>
        get() = _errorSave

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(
            object : PostRepository.Callback<List<Post>> {
                override fun onSuccess(result: List<Post>) {
                    _data.value = FeedModel(posts = result, empty = result.isEmpty())
                }

                override fun onError(exception: Exception) {
                    _errorLoad.value = Exception(exception)
                }
            }
        )
    }

    fun likeById(id: Long) {
        val post = _data.value?.posts.orEmpty().find { it.id == id }
        post?.let {
            val likedByMe = post.likedByMe
            repository.likeByIdAsync(id, likedByMe, object : PostRepository.Callback<Post> {
                override fun onSuccess(result: Post) {
                    _data.postValue(
                        _data.value?.copy(posts = _data.value?.posts.orEmpty()
                            .map {
                                if (it.id == id) {
                                    result
                                } else it
                            }
                        ))
                }

                override fun onError(exception: Exception) {
                    _data.value = FeedModel(error = true)
                }
            })
        }
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            ))
        if (_data.value!!.posts.isEmpty()) _data.postValue(FeedModel(empty = true))
        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {
            }

            override fun onError(exception: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContentAndSave(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value?.let {
            repository.saveAsync(it.copy(content = text),
                object : PostRepository.Callback<Post> {

                    override fun onError(exception: Exception) {
                        requireNotNull(edited.value).content = text
                        _errorSave.value = Exception(exception)
                    }

                    override fun onSuccess(result: Post) {
                        _postCreated.postValue(Unit)
                        _errorSave = SingleLiveEvent()
                        edited.value = empty
                    }
                })
        }
    }

    fun saveAsync() {
        edited.value?.let {
            repository.saveAsync(
                it, object : PostRepository.Callback<Post> {
                    override fun onError(exception: Exception) {
                        requireNotNull(edited.value).content = it.content
                        _errorSave.value = Exception(exception)
                    }

                    override fun onSuccess(result: Post) {
                        _postCreated.postValue(Unit)
                        _errorSave = SingleLiveEvent()
                        edited.value = empty
                    }
                })
        }
    }

    fun cancel() {
        edited.value?.let {
            edited.value = empty
        }
    }
}