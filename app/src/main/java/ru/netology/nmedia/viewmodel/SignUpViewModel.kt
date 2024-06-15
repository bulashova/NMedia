package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.File

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    val auth: LiveData<Token?> = AppAuth.getInstance().state
        .asLiveData()
    val authenticated: Boolean
        get() = auth.value?.token != null

    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val noPhoto = PhotoModel()

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun registerUser(login: String, pass: String, name: String) = viewModelScope.launch {
        try {
            repository.registerUser(login, pass, name)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun registerWithPhoto(login: String, pass: String, name: String) = viewModelScope.launch {
        try {
            when (_photo.value) {
                noPhoto -> {
                    repository.registerUser(
                        login,
                        pass,
                        name
                    )
                }

                else -> _photo.value?.file?.let { file ->
                    repository.registerWithPhoto(login, pass, name, MediaUpload(file))
                }
            }
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
        dropPhoto()
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun dropPhoto() {
        _photo.value = noPhoto
    }
}