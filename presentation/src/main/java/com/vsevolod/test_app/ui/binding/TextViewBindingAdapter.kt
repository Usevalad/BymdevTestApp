package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.vsevolod.test_app.ui.utils.printByStyle
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


@BindingAdapter("optionalText")
fun TextView.setOptionalText(textRes: Int) {
    text = when (textRes) {
        0 -> null
        else -> context.getString(textRes)
    }
}

@BindingAdapter(
        value = [
            "styledText",
            "highlightedFragment",
            "highlightColor",
            "highlightClick",
            "underline",
            "bold",
            "asHtml"
        ],
        requireAll = false
)
fun TextView.setHighlightedFragment(
        styledText: String?,
        highlightedFragment: String?,
        highlightColor: Int,
        highlightClick: View.OnClickListener?,
        underline: Boolean,
        bold: Boolean,
        asHtml: Boolean
) {
    if (styledText == null && highlightedFragment == null) {
        throw IllegalStateException()
    }

    val totalText = when (highlightedFragment) {
        null -> styledText!!
        else -> String.format(styledText ?: "%s", highlightedFragment)
    }

    val builder = SpannableStringBuilder(
            totalText.takeUnless { asHtml } ?: Html.fromHtml(totalText)
    )

    builder.setClickableFragment(
            highlightedFragment ?: styledText!!,
            highlightColor,
            highlightClick,
            underline,
            bold
    )

    movementMethod = LinkMovementMethod.getInstance()
    text = builder
}

private fun SpannableStringBuilder.setClickableFragment(
        textToStyle: String,
        highlightColor: Int,
        highlightClick: View.OnClickListener?,
        underline: Boolean,
        bold: Boolean
) {
    val span = object : ClickableSpan() {

        override fun onClick(widget: View) {
            highlightClick?.onClick(widget)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = underline
            ds.isFakeBoldText = bold
            ds.color = highlightColor
        }
    }

    val start = indexOf(textToStyle)
    setSpan(span, start, start + textToStyle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

@BindingAdapter(
        value = [
            "textWithDate",
            "date",
            "dateStyle",
            "datePattern",
            "dateColor",
            "dateUnderline",
            "dateBold",
            "asHtml"
        ],
        requireAll = false
)
fun TextView.setHighlightedDate(
        textWithDate: String?,
        date: DateTime?,
        dateStyle: String?,
        datePattern: String?,
        dateColor: Int,
        dateUnderline: Boolean,
        dateBold: Boolean,
        asHtml: Boolean
) {
    if (datePattern != null && dateStyle != null) {
        throw IllegalStateException()
    }

    when {
        dateStyle != null -> date.printByStyle(dateStyle)

        datePattern != null -> DateTimeFormat.forPattern(datePattern).print(date)

        else -> date.printByStyle("MM")

    }.let { formattedDate ->
        date?.let {
            setHighlightedFragment(
                    textWithDate,
                    formattedDate,
                    dateColor,
                    null,
                    dateUnderline,
                    dateBold,
                    asHtml
            )
        }
    }
}
