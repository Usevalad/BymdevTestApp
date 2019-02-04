package com.vsevolod.test_app.ui.screens

import android.os.Bundle
import com.vsevolod.core.entities.User
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.bases.BaseViewModel
import com.vsevolod.test_app.ui.bases.ViewModelFragment
import javax.inject.Inject

class UserDetailFragment : ViewModelFragment<UserDetailViewModel>() {

    override val viewModelClass = UserDetailViewModel::class.java
    override val layoutId = R.layout.fragment_user_details

    val user get() = arguments!!.getSerializable(ARG_USER) as User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.init(user)
        }
    }


    companion object {
        const val ARG_USER = "ARG_USER"
    }
}


class UserDetailViewModel @Inject constructor() : BaseViewModel() {

    lateinit var user: User

    fun init(user: User) {
        this.user = user
    }
}