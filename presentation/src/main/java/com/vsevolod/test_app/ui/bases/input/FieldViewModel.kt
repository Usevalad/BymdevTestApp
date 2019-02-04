package com.vsevolod.test_app.ui.bases.input

import android.databinding.*
import android.text.*
import android.util.Patterns
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import com.vsevolod.test_app.BR
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.bases.input.FieldViewModel.ConditionChecker
import com.vsevolod.test_app.ui.binding.MoveCursorToEnd
import com.vsevolod.test_app.ui.binding.RequestFocus
import kotlin.properties.Delegates


data class FieldViewModel(
        val hint: Int,
        val inputType: Int = InputType.TYPE_CLASS_TEXT,
        val maxLines: Int = 1,
        val minLines: Int = 1,
        val maxLength: Int = -1,
        val optionality: Optionality = Optionality.Required(),
        val counterEnabled: Boolean = false,
        val gravity: Int = Gravity.START,
        private val additionalFilters: Set<InputFilter> = emptySet(),
        private val availableChars: String? = null,
        private val onFocused: (() -> Unit)? = null,
        private val onFocusLost: (() -> Unit)? = null,
        private val conditions: Set<ConditionChecker> = emptySet(),
        private val onImeActionDone: () -> Unit = {},
        private val onImeActionNext: () -> Unit = {}
) : BaseObservable(), Validable {

    val input = ObservableField<String>("")

    val error = ObservableInt()
    val enabled = ObservableBoolean(true)
    val hasFocus = ObservableBoolean()

    val requestFocus = ObservableField<RequestFocus>()
    val moveCursorToEnd = ObservableField<MoveCursorToEnd>()

    val filters by lazy { (additionalFilters + internalFilters).toTypedArray() }

    var value
        get() = input.get()!!
        set(value) {
            input.set(value)
            moveCursorToEnd()
        }

    var onClick by Delegates.observable<(() -> Unit)?>(null) { _, _, _ ->
        notifyPropertyChanged(BR.clickable)
        notifyPropertyChanged(BR.focusable)
    }

    val clickable @Bindable get() = onClick != null
    val focusable @Bindable get() = onClick == null

    private val internalFilters = setOf(
            availableChars?.let(FieldViewModel::WrongCharsFilter),
            maxLength.takeIf { it > 0 }?.let(InputFilter::LengthFilter)
    ).filterNotNull()

    override fun validate(): Boolean {

        error.set(0)

        return if (value.trim().isEmpty()) {

            when (optionality) {

                is Optionality.Required -> {
                    error.set(optionality.error)
                    false
                }

                else -> true
            }

        } else {
            conditions.find { !it.check(value) }
                    ?.error
                    ?.also(error::set) == null
        }
    }

    fun onFocused() {
        hasFocus.set(true)
        onFocused?.invoke()
    }

    fun onFocusLost() {
        hasFocus.set(false)
        onFocusLost?.invoke()
    }

    fun onTextChanged() {
        error.set(0)
    }

    fun moveCursorToEnd() {
        moveCursorToEnd.set(MoveCursorToEnd)
    }

    fun requestFocus() {
        requestFocus.set(RequestFocus)
    }

    fun onEditorAction(actionId: Int): Boolean {

        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> onImeActionDone
            EditorInfo.IME_ACTION_NEXT -> onImeActionNext
            else -> null
        }?.invoke()

        return false
    }


    class ConditionChecker(
            val error: Int,
            val check: CharSequence.() -> Boolean
    )


    sealed class Optionality {

        class Required(
                val error: Int = R.string.error_field_required
        ) : Optionality()

        object Optional : Optionality()
    }


    private class WrongCharsFilter(val chars: String) : InputFilter {

        override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
        ): CharSequence? {

            var keepOriginal = true
            val builder = StringBuilder(end - start)

            for (i in start until end) {
                val c = source[i]

                when {
                    chars.contains(c) -> builder.append(c)
                    else -> keepOriginal = false
                }
            }

            return when {
                keepOriginal -> null

                source is Spanned -> SpannableString(builder).also {
                    TextUtils.copySpansFrom(source, start, builder.length, null, it, 0)
                }

                else -> builder
            }
        }
    }
}

fun email() = FieldViewModel(
        hint = R.string.hint_email,
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        conditions = setOf(
                ConditionChecker(R.string.error_wrong_email) {
                    Patterns.EMAIL_ADDRESS.matcher(this).matches()
                }
        )
)

fun password(hint: Int = R.string.hint_password) = FieldViewModel(
        hint = hint,
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
        conditions = setOf(
                ConditionChecker(R.string.error_wrong_password) { length >= 8 }
        )
)

fun confirmPassword(newPassword: FieldViewModel) = FieldViewModel(
        hint = R.string.hint_confirm_password,
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
        conditions = setOf(
                ConditionChecker(R.string.error_password_confirmation) { equals(newPassword.value) }
        )
)
