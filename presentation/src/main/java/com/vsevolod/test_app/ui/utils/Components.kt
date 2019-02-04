package com.vsevolod.test_app.ui.utils

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

inline fun <reified VM : ViewModel> ViewModelProvider.get() = get(VM::class.java)
