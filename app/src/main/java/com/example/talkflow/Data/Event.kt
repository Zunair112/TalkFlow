package com.example.talkflow.Data

open class Event<out T>(val content: T) {
    var hasBeenHandled = false
    fun getContentOrNUll(): T? {
        return if (hasBeenHandled) null
        else {
            hasBeenHandled = true
            content
        }
    }
}