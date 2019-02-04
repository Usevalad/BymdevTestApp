package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.support.v4.widget.SwipeRefreshLayout


@InverseBindingAdapter(attribute = "refreshing", event = "refreshingAttrChanged")
fun SwipeRefreshLayout.getRefreshing() = isRefreshing

@BindingAdapter("refreshingAttrChanged")
fun SwipeRefreshLayout.setRefreshingAttrChanged(attrChanged: InverseBindingListener?) {
    setOnRefreshListener { attrChanged?.onChange() }
}

@BindingAdapter("refreshing")
fun SwipeRefreshLayout.doSetRefreshing(refreshing: Boolean) {
    isRefreshing = refreshing
}

@BindingAdapter("indicatorColor")
fun SwipeRefreshLayout.setColorSchemaColor(indicatorColor: Int) {
    setColorSchemeColors(indicatorColor)
}
