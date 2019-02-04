package com.vsevolod.test_app.internal.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.vsevolod.core.gateways.LocalStorage
import com.vsevolod.core.gateways.NetworkFacade
import com.vsevolod.data.LocalStorageImpl
import com.vsevolod.data.NetworkFacadeImpl
import com.vsevolod.test_app.Application
import com.vsevolod.test_app.internal.impls.ToastManagerImpl
import com.vsevolod.test_app.internal.impls.ViewModelFactoryImpl
import com.vsevolod.test_app.ui.bases.BaseViewModel
import com.vsevolod.test_app.ui.screens.*
import com.vsevolod.test_app.ui.screens.initial.SplashActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideCicerone(): Cicerone<Router> = Cicerone.create()

    @Provides
    fun provideNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
            cicerone.navigatorHolder

    @Provides
    fun provideRouter(cicerone: Cicerone<Router>): Router = cicerone.router
}


@Module
interface ImplementationsModule {
    @Binds fun context(application: Application): Context
    @Binds fun networkFacade(networkFacade: NetworkFacadeImpl): NetworkFacade
    @Binds fun localStorage(LocalStorage: LocalStorageImpl): LocalStorage
    @Binds fun viewModelFactory(viewModelFactory: ViewModelFactoryImpl): ViewModelProvider.Factory
    @Binds fun toastManager(toastManager: ToastManagerImpl): BaseViewModel.ToastManager
}


@Module
interface ActivitiesModule {
    @ContributesAndroidInjector fun splash(): SplashActivity
    @ContributesAndroidInjector fun main(): MainActivity
}


@Module
interface FragmentsModule {
    @ContributesAndroidInjector fun userList(): UserListFragment
    @ContributesAndroidInjector fun userDetails(): UserDetailFragment
}


/**
 * Example:
 * @Binds
 * @IntoMap
 * @ViewModelKey(SignInViewModel::class)
 * fun signIn(viewModel: SignInViewModel): ViewModel
 */
@Module
interface ViewModelsModule {
     @Binds
     @IntoMap
     @ViewModelKey(UserListViewModel::class)
     fun userList(viewModel: UserListViewModel): ViewModel

     @Binds
     @IntoMap
     @ViewModelKey(UserDetailViewModel::class)
     fun userDetails(viewModel: UserDetailViewModel): ViewModel
}
