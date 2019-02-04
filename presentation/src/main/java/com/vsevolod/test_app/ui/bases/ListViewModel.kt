package com.vsevolod.test_app.ui.bases

import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.databinding.ViewDataBinding
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.vsevolod.core.LazyRepo
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.utils.setViewModel
import io.reactivex.Single
import org.jetbrains.anko.layoutInflater
import kotlin.properties.Delegates


abstract class ListViewModel<I : Any> : BaseViewModel() {

    val noItemsMessage = ObservableInt()
    val adapter: RecyclerView.Adapter<*> by lazy { onCreateRecyclerViewAdapter() }

    protected open val emptyListText = R.string.empty_list
    protected open val errorText = R.string.error_loading

    var list by Delegates.observable(emptyList<I>()) { _, old, new -> processListChanged(old, new) }

    protected abstract fun onCreateRecyclerViewAdapter(): BaseAdapter
    protected abstract fun onListChange(old: List<I>, new: List<I>): BaseDiffUtilCallback

    protected fun notifyUpdateFailed() {

        if (list.isEmpty()) {
            noItemsMessage.set(errorText)
        }

        onListUpdateTerminated()
    }

    protected open fun onListUpdateTerminated() {}

    protected fun setItemAt(position: Int, newValue: I) {
        mutateList { it[position] = newValue }
    }

    protected fun removeItemAt(position: Int) = mutateList { it.removeAt(position) }

    private fun <T> mutateList(block: (MutableList<I>) -> T): T {
        val mutable = list.toMutableList()
        val result = block(mutable)
        list = mutable
        return result
    }

    private fun processListChanged(old: List<I>, new: List<I>) {

        DiffUtil.calculateDiff(onListChange(old, new)).dispatchUpdatesTo(adapter)
        noItemsMessage.set(emptyListText.takeIf { new.isEmpty() } ?: 0)

        onListUpdateTerminated()
    }


    abstract inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val item = ObservableField<I>()
        protected val entry get() = item.get()!!

        protected fun mutateItem(newValue: I.() -> I) {
            if (adapterPosition >= 0) {
                setItemAt(adapterPosition, newValue(entry))
            }
        }
    }


    abstract inner class BaseAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        protected abstract val itemLayoutId: Int
        protected abstract fun onCreateItemViewHolder(v: View): ItemViewHolder

        final override fun getItemCount() = list.size

        final override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ) = DataBindingUtil.inflate<ViewDataBinding>(
                parent.context.layoutInflater,
                itemLayoutId,
                parent,
                false
        ).let { binding ->
            onCreateItemViewHolder(binding.root).also(binding::setViewModel)
        }

        final override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.item.set(list[position])
        }
    }


    protected abstract inner class BaseDiffUtilCallback : DiffUtil.Callback() {

        protected abstract val old: List<I>
        protected abstract val new: List<I>

        final override fun getNewListSize() = new.size
        final override fun getOldListSize() = old.size

        abstract fun areItemsTheSame(i1: I, i2: I): Boolean
        abstract fun areContentsTheSame(i1: I, i2: I): Boolean

        final override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
        ) = areItemsTheSame(old[oldItemPosition], new[newItemPosition])

        final override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
        ) = areContentsTheSame(old[oldItemPosition], new[newItemPosition])
    }
}


abstract class RefreshableListViewModel<I : Any> : ListViewModel<I>(), WithRefresh {

    final override val refreshable: Refreshable by lazy { RefreshableImpl() }

    protected abstract fun getList(): Single<List<I>>


    private inner class RefreshableImpl : Refreshable() {

        override fun onRefresh() {
            getList().execute {
                progressBehavior = ProgressBehavior.Refresher
                onSuccess { list = it }
                onError(::notifyUpdateFailed)
            }
        }
    }
}


abstract class LazyListViewModel<I : Any> : ListViewModel<I>(), WithRefresh {

    override val refreshable: Refreshable by lazy {
        LazyRefreshable(
                itemsRepo,
                { list = it },
                ::notifyUpdateFailed
        )
    }

    abstract val itemsRepo: LazyRepo<List<I>>
}
