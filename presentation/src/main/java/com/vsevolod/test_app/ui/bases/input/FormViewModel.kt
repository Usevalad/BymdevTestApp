package com.vsevolod.test_app.ui.bases.input

import com.vsevolod.test_app.ui.bases.BaseViewModel

interface Validable {
    fun validate(): Boolean
}


abstract class FormViewModel : BaseViewModel() {

    protected abstract val fields: List<Validable>

    protected abstract fun onInputComplete()

    fun attemptToComplete() {
        if (validate()) {
            onInputComplete()
        }
    }

    fun validate(silently: Boolean = false) = fields.reversed()
            .asSequence()
            .lastOrNull { !it.validate() }
            ?.also {
                if (!silently) {
                    (it as? FieldViewModel)?.requestFocus()
                }
            } == null
}
