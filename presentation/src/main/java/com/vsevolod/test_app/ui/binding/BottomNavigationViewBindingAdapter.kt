package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.support.design.widget.BottomNavigationView
import android.view.MenuItem
import com.vsevolod.test_app.R

@InverseBindingAdapter(
        attribute = "selectedItemId",
        event = "selectedItemIdAttrChanged"
)
fun BottomNavigationView.doGetSelectedItemId() =
        getTag(R.id.selected_item_id_actual) as? Int ?: selectedItemId

@BindingAdapter("selectedItemId")
fun BottomNavigationView.doSetSelectedItemId(id: Int) {
    if (id != selectedItemId) {
        selectedItemId = id
    }
}


interface OnSelectedItemChangeListener {
    fun onSelectedItemIdChange(id: Int)
}

@BindingAdapter(
        value = [
            "selectedItemIdAttrChanged",
            "selectedItemIdChange"
        ],
        requireAll = false
)
fun BottomNavigationView.doSetSelectedItemIdAttrChanged(
        selectedItemIdAttrChanged: InverseBindingListener?,
        onSelectedItemIdChange: OnSelectedItemChangeListener?
) {
    val listener = OnSelectedItemChangeListenerImpl(
            this,
            selectedItemIdAttrChanged,
            onSelectedItemIdChange
    )

    setOnNavigationItemSelectedListener(listener)
}


private class OnSelectedItemChangeListenerImpl(
        private val owner: BottomNavigationView,
        private val selectedItemIdAttrChanged: InverseBindingListener? = null,
        private val onSelectedItemIdChangeListener: OnSelectedItemChangeListener? = null
) : BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        owner.setTag(R.id.selected_item_id_actual, item.itemId)

        selectedItemIdAttrChanged?.onChange()
        onSelectedItemIdChangeListener?.onSelectedItemIdChange(item.itemId)

        return true
    }
}
