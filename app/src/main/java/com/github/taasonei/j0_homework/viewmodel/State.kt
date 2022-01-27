package com.github.taasonei.j0_homework.viewmodel

sealed class State<out T> {
    class Loading<out T> : State<T>()
    data class Success<out T>(val data: T) : State<T>()
    data class Error<out T>(val error: String) : State<T>()
}