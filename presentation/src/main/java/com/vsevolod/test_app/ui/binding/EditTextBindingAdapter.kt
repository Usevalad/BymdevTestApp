package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.widget.EditText


object MoveCursorToEnd

@Suppress("unused")
@InverseBindingAdapter(attribute = "moveCursorToEnd", event = "moveCursorToEndAttrChanged")
fun EditText.notifyCursorMovedToNext(): MoveCursorToEnd? = null

@BindingAdapter(value = ["moveCursorToEnd", "moveCursorToEndAttrChanged"], requireAll = false)
fun EditText.doMoveCursorToNext(command: MoveCursorToEnd?, onChanged: InverseBindingListener?) {
    command?.apply {
        setSelection(text.length)
        onChanged?.onChange()
    }
}
