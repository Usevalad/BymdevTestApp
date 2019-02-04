package com.vsevolod.test_app.ui.bases

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.vsevolod.test_app.ui.utils.withViewModel
import dagger.android.support.DaggerAppCompatDialogFragment
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.support.v4.ctx

abstract class BaseDialogFragment : DaggerAppCompatDialogFragment() {

    protected open val cancelable get() = true

    protected abstract fun onCreateAlertView(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = cancelable
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(ctx)
                    .setView(onCreateAlertView())
                    .create()
}


abstract class BaseBindingDialogFragment : BaseDialogFragment() {

    protected open val viewModel: Any get() = this

    abstract val layoutId: Int

    final override fun onCreateAlertView(): View =
            DataBindingUtil
                    .inflate<ViewDataBinding>(ctx.layoutInflater, layoutId, null, false)
                    .withViewModel(viewModel)
                    .root
}
