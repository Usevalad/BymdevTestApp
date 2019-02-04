package com.vsevolod.test_app.ui.bases

import android.app.Dialog
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.utils.color
import com.vsevolod.test_app.ui.utils.get
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.withArguments

class ProgressDialogFragment : DialogFragment() {

    lateinit var disposable: Disposable

    private lateinit var viewModel: ProgressDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get()
        isCancelable = false

        if (savedInstanceState == null) {
            viewModel.disposable = disposable
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(ctx)
                    .setView(makeView())
                    .create()

    private fun makeView() = ctx.linearLayout {

        gravity = Gravity.CENTER_VERTICAL
        padding = dimen(R.dimen.margin_small)

        progressBar().lparams {
            margin = dimen(R.dimen.margin_middle)
        }

        textView(R.string.loading) {
            textColor = R.color.black
        }

        space().lparams(
                width = wrapContent,
                height = matchParent,
                weight = 1f
        )

        button(text = android.R.string.cancel) {
            backgroundResource = color(android.R.color.transparent)
            visibility = View.VISIBLE.takeIf { arguments?.getBoolean(ARG_CANCELABLE) == true }
                    ?: View.GONE

            setOnClickListener {
                toast(R.string.canceled)
                dismiss()
            }
        }
    }


    companion object {
        const val ARG_CANCELABLE = "ARG_CANCELABLE"
    }
}


class ProgressDialogViewModel : ViewModel() {

    lateinit var disposable: Disposable

    override fun onCleared() {
        disposable.dispose()
    }
}


fun AppCompatActivity.showProgress(disposable: Disposable, cancelable: Boolean) {
    ProgressDialogFragment()
            .also { it.disposable = disposable }
            .withArguments(ProgressDialogFragment.ARG_CANCELABLE to cancelable)
            .show(supportFragmentManager, Screens.PROGRESS)
}
