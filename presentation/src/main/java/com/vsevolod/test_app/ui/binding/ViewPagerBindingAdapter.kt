package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.databinding.adapters.ListenerUtil
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import com.vsevolod.test_app.R

@InverseBindingAdapter(
        attribute = "currentItemPosition",
        event = "currentItemPositionAttrChanged"
)
fun ViewPager.doGetCurrentItemPosition() = currentItem

@BindingAdapter("currentItemPosition", "adapter")
fun ViewPager.doSetCurrentItemPosition(position: Int, adapter: PagerAdapter) {

    if (this.adapter != adapter) {
        this.adapter = adapter
    }

    if (currentItem != position) {
        currentItem = position
    }
}


interface OnCurrentItemPositionChangeListener {
    fun onCurrentItemPositionChange(position: Int)
}

@BindingAdapter(
        value = [
            "currentItemPositionAttrChanged",
            "onCurrentItemPositionChange"
        ],
        requireAll = false
)
fun ViewPager.doSetCurrentItemPositionAttrChanged(
        currentItemPositionAttrChanged: InverseBindingListener?,
        onCurrentItemPositionChange: OnCurrentItemPositionChangeListener?
) {
    var listener = ListenerUtil.getListener<OnPageChangeListenerImpl>(
            this,
            R.id.on_page_change_listener
    )

    if (listener == null) {
        listener = OnPageChangeListenerImpl().also(::addOnPageChangeListener)

        ListenerUtil.trackListener(
                this,
                listener,
                R.id.on_page_change_listener
        )?.let(::removeOnPageChangeListener)
    }

    listener.currentItemPositionAttrChanged = currentItemPositionAttrChanged
    listener.onCurrentItemPositionChangeListener = onCurrentItemPositionChange
}


private class OnPageChangeListenerImpl : ViewPager.SimpleOnPageChangeListener() {

    var currentItemPositionAttrChanged: InverseBindingListener? = null
    var onCurrentItemPositionChangeListener: OnCurrentItemPositionChangeListener? = null

    override fun onPageSelected(position: Int) {
        currentItemPositionAttrChanged?.onChange()
        onCurrentItemPositionChangeListener?.onCurrentItemPositionChange(position)
    }
}
