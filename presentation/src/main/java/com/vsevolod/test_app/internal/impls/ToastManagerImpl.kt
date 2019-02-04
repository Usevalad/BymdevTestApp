package com.vsevolod.test_app.internal.impls

import android.content.Context
import com.vsevolod.test_app.ui.bases.BaseViewModel
import org.jetbrains.anko.toast
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastManagerImpl @Inject constructor(
        private val context: Context
) : BaseViewModel.ToastManager {

    override fun showToast(message: Int) {
        context.toast(message)
    }
}
