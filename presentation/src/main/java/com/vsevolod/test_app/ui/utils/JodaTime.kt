package com.vsevolod.test_app.ui.utils

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

fun DateTime?.printByStyle(style: String): String =
        this?.let(DateTimeFormat.forStyle(style)::print).orEmpty()

fun LocalDate?.printByStyle(style: String): String =
        this?.let(DateTimeFormat.forStyle(style)::print).orEmpty()
