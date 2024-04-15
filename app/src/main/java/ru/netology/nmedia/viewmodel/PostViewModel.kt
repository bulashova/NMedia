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
    published = ""
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

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(
            object : PostRepository.Callback<List<Post>> {
                override fun onSuccess(result: List<Post>) {
                    _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
                }

                override fun onError(exception: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            }
        )
    }

    fun likeById(id: Long) {
        val post = _data.value?.posts.orEmpty().find { it.id == id }
        post?.let {
            repository.likeByIdAsync(id, object : PostRepository.Callback<Post> {
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
                    _data.postValue(FeedModel(error = true))
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
                _data.postValue(_data.value?.copy(posts = old))
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
            repository.saveAsync(it.copy(content = text), object : PostRepository.Callback<Post> {
                override fun onSuccess(result: Post) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(exception: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
        edited.value = empty
    }

    fun cancel() {
        edited.value?.let {
            edited.value = empty
        }
    }
}