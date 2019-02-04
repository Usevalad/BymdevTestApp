package com.vsevolod.test_app.ui.bases.pickers

import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.bases.input.FieldViewModel
import com.vsevolod.test_app.ui.bases.input.Validable
import com.vsevolod.test_app.ui.utils.printByStyle
import org.joda.time.LocalDate
import ru.terrakok.cicerone.Router

class DatePicker(
        hint: Int,
        val router: Router,
        val minDate: LocalDate? = null,
        val maxDate: LocalDate? = null,
        private val required: Boolean = true
) : Validable {

    var pickedDate: LocalDate? = null

    val field = FieldViewModel(hint)

    init {
        field.onClick = { router.navigateTo(Screens.DATE_PICKER, this) }
    }

    fun init(date: LocalDate?) {
        field.input.set(date.printByStyle("L-"))
    }

    fun notifyDatePicked(value: LocalDate) {
        pickedDate = value
        init(value)
    }

    override fun validate() = !required || field.validate()
}
