package com.vsevolod.test_app.ui.screens

import android.os.Bundle
import com.vsevolod.core.entities.User
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.bases.BaseActivity
import org.jetbrains.anko.support.v4.withArguments
import ru.terrakok.cicerone.Navigator

class MainActivity : BaseActivity() {

    override val layoutId = R.layout.fragment_container
    override val navigator: Navigator = NavigatorImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: router.replaceScreen(Screens.MAIN_USER_LIST)
    }


    private inner class NavigatorImpl : BaseNavigator() {
        override fun createFragment(screenKey: String, data: Any?) =
                when (screenKey) {
                    Screens.MAIN_USER_LIST -> UserListFragment()
                    Screens.MAIN_USER_DETAILS -> UserDetailFragment().withArguments(
                            UserDetailFragment.ARG_USER to data as User
                    )

                    else -> super.createFragment(screenKey, data)
                }
    }
}
