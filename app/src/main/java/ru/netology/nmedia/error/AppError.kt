package ru.netology.nmedia.error

import java.lang.RuntimeException

sealed class AppError(var code: String): RuntimeException()
class ApiError(val status: Int, code: String): AppError(code)
object NetworkError : AppError("error_network")
object UnknownError: AppError("error_unknown")