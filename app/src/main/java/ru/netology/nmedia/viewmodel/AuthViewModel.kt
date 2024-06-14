package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token

class AuthViewModel : ViewModel() {
    val auth: LiveData<Token?> = AppAuth.getInstance().state
        .asLiveData()
    val authenticated: Boolean
        get() = auth.value?.token != null
}