package com.vsevolod.test_app.ui

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEvents @Inject constructor() {

    private val listeners = mutableSetOf<Listener>()

    fun addListener(l: Listener) {
        listeners.add(l)
    }

    fun removeListener(l: Listener) {
        listeners.remove(l)
    }

    fun notifyListeners(notification: Listener.() -> Unit) {
        listeners.forEach(notification)
    }


    interface Listener
}
