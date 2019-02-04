package com.vsevolod.test_app.internal.di

import com.vsevolod.data.internal.di.ApiModule
import com.vsevolod.test_app.Application
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AppModule::class,
            ApiModule::class,
            ImplementationsModule::class,
            ActivitiesModule::class,
            FragmentsModule::class,
            ViewModelsModule::class,
            AndroidInjectionModule::class,
            AndroidSupportInjectionModule::class
        ]
)
interface AppComponent : AndroidInjector<Application> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<Application>()
}
