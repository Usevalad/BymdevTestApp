package com.vsevolod.test_app.ui.utils

import android.arch.lifecycle.LifecycleOwner
import android.databinding.ObservableInt
import android.databinding.ViewDataBinding
import com.vsevolod.test_app.BR

fun <VDB : ViewDataBinding> VDB.withViewModel(
        viewModel: Any?,
        lifecycleOwner: LifecycleOwner? = null
) = apply {
    if (!setVariable(BR.viewModel, viewModel)) {
        throw IllegalStateException()
    }

    setLifecycleOwner(lifecycleOwner)
}

fun ViewDataBinding.setViewModel(viewModel: Any?, lifecycleOwner: LifecycleOwner) {
    withViewModel(viewModel, lifecycleOwner)
}

fun ViewDataBinding.setViewModel(viewModel: Any?) {
    withViewModel(viewModel)
}

fun ObservableInt.dec() = set(get() - 1)

fun ObservableInt.inc() = set(get() + 1)
