package com.vsevolod.test_app

import com.vsevolod.test_app.internal.di.DaggerAppComponent
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class Application : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
    }

    override fun applicationInjector(): AndroidInjector<Application> =
            DaggerAppComponent.builder().create(this)

}
