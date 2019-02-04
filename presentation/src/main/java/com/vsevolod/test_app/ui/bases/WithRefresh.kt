package com.vsevolod.test_app.ui.bases

import android.databinding.Observable
import android.databinding.ObservableBoolean


interface WithRefresh {
    val refreshable: Refreshable
}

/* not overridable */
fun WithRefresh.refresh() {
    refreshable.refresh()
}

/* not overridable */
fun WithRefresh.notifyRefreshTerminated() {
    refreshable.notifyRefreshTerminated()
}

abstract class Refreshable {

    val refreshing = ObservableBoolean()

    init {
        refreshing.addOnPropertyChangedCallback(OnRefreshingChangedCallback())
    }

    protected abstract fun onRefresh()

    fun refresh() {
        refreshing.set(true)
    }

    fun notifyRefreshTerminated() {
        refreshing.set(false)
    }


    private inner class OnRefreshingChangedCallback : Observable.OnPropertyChangedCallback() {

        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (refreshing.get()) {
                onRefresh()
            }
        }
    }
}
