package com.vsevolod.test_app.ui.screens.initial

import android.os.Bundle
import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.bases.BaseActivity

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router.replaceScreen(Screens.MAIN)
    }
}
