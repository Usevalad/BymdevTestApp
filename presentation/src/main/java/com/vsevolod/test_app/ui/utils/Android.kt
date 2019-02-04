package com.vsevolod.test_app.ui.utils

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.View
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.ctx


fun Context.showSimpleAlert(message: String) = alert(message) { okButton {} }.show()
fun Context.showSimpleAlert(messageId: Int) = alert(messageId) { okButton {} }.show()

fun Context.drawable(drawableId: Int) = ContextCompat.getDrawable(this, drawableId)!!
fun View.drawable(drawableId: Int) = context.drawable(drawableId)
fun Fragment.drawable(drawableId: Int) = ctx.drawable(drawableId)

fun Context.color(colorId: Int) = ContextCompat.getColor(this, colorId)
fun View.color(colorId: Int) = context.color(colorId)
fun Fragment.color(colorId: Int) = ctx.color(colorId)

fun Context.colorStateList(id: Int) = ContextCompat.getColorStateList(this, id)
fun View.colorStateList(id: Int) = context.colorStateList(id)
fun Fragment.colorStateList(id: Int) = ctx.colorStateList(id)
