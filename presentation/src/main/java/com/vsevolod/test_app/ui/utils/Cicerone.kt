package com.vsevolod.test_app.ui.utils

import ru.terrakok.cicerone.commands.BackTo
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import ru.terrakok.cicerone.commands.Replace

fun Command.screenKey() =
        when (this) {
            is BackTo -> screenKey
            is Replace -> screenKey
            is Forward -> screenKey
            else -> null
        }

inline fun <reified T> Command.transitionData() =
        when (this) {
            is Replace -> transitionData
            is Forward -> transitionData
            else -> null
        } as T