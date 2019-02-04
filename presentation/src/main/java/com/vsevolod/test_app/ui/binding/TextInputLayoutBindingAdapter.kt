package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.support.design.widget.TextInputLayout
import android.view.View


@BindingAdapter("error")
fun TextInputLayout.setError(errorRes: Int) {
    error = when (errorRes) {
        0 -> null
        else -> resources.getString(errorRes)
    }
}

@BindingAdapter("inputType", "passwordToggleEnabled")
fun TextInputLayout.setInputType(inputType: Int, passwordToggleEnabled: Boolean) {
    // do not call those setters from a data binding layout:
    // the specified order is required
    editText!!.inputType = inputType
    isPasswordVisibilityToggleEnabled = passwordToggleEnabled
}


@BindingAdapter("hint", "optional")
fun TextInputLayout.setHint(hint: Int, optional: Boolean) {
    this.hint = makeHint(hint, optional)
}

private fun View.makeHint(hint: Int, optional: Boolean) =
        when (hint) {
            0 -> null
            else -> resources.getString(hint) + (" *".takeIf { !optional } ?: "")
        }
