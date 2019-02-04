package com.vsevolod.test_app.ui.screens

import android.view.View
import com.vsevolod.core.entities.User
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.bases.ListViewModel
import com.vsevolod.test_app.ui.bases.ViewModelFragment
import javax.inject.Inject

class UserListFragment : ViewModelFragment<UserListViewModel>() {
    override val layoutId = R.layout.list_content
    override val viewModelClass = UserListViewModel::class.java
}


class UserListViewModel @Inject constructor() : ListViewModel<User>() {
    override fun onCreateRecyclerViewAdapter(): BaseAdapter = UserAdapter()
    override fun onListChange(old: List<User>, new: List<User>): BaseDiffUtilCallback = DiffCallbackImpl(old, new)

    @Inject
    fun initList() {
        facade.users()
                .execute {
                    progressBehavior = ProgressBehavior.PreLoader

                    onSuccess {
                        list = it
                    }

                    onError {
                        list = listOf()
                    }
                }
    }

    inner class UserAdapter : BaseAdapter() {

        override val itemLayoutId = R.layout.item_user

        override fun onCreateItemViewHolder(v: View) = UserItemViewHolder(v)


        inner class UserItemViewHolder(v: View) : ItemViewHolder(v) {

            fun showDetails() {
                router.navigateTo(Screens.MAIN_USER_DETAILS, list[adapterPosition])
            }
        }
    }


    private inner class DiffCallbackImpl(
            override val old: List<User>,
            override val new: List<User>
    ) : BaseDiffUtilCallback() {
        override fun areItemsTheSame(i1: User, i2: User) = i1.id == i2.id
        override fun areContentsTheSame(i1: User, i2: User) = i1 == i2
    }
}