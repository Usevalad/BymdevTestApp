package com.vsevolod.test_app.ui.binding

import android.databinding.BindingConversion
import android.view.View

@BindingConversion
fun booleanToVisibility(visible: Boolean) = View.VISIBLE.takeIf { visible } ?: View.GONE
