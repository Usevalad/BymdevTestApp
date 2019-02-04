package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.view.View


object RequestFocus

@BindingAdapter(value = ["requestFocus", "requestFocusAttrChanged"], requireAll = false)
fun View.doRequestFocus(
        command: RequestFocus?,
        attrChange: InverseBindingListener?) {

    command?.let {
        requestFocus()
        attrChange?.onChange()
    }
}

@Suppress("unused")
@InverseBindingAdapter(attribute = "requestFocus", event = "requestFocusAttrChanged")
fun View.notifyFocusRequested(): RequestFocus? = null
