package com.vsevolod.test_app.ui.bases

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.support.annotation.CallSuper
import com.vsevolod.core.LazyRepo
import com.vsevolod.core.ReloadCriteria
import com.vsevolod.core.gateways.LocalStorage
import com.vsevolod.core.gateways.NetworkFacade
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.AppEvents
import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.utils.dec
import com.vsevolod.test_app.ui.utils.inc
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class BaseViewModel : ViewModel(), AppEvents.Listener {

    @Inject lateinit var facade: NetworkFacade
    @Inject lateinit var storage: LocalStorage

    @Inject lateinit var events: AppEvents

    @Inject lateinit var router: Router
    @Inject lateinit var toastManager: ToastManager

    val loadings = ObservableInt()

    private val disposables = CompositeDisposable()

    @Inject
    fun bindToEvents() {
        events.addListener(this)
    }

    @CallSuper
    override fun onCleared() {
        disposables.dispose()
        events.removeListener(this)
    }

    protected fun showToast(message: Int) {
        toastManager.showToast(message)
    }

    fun onBackPressed() = false

    protected fun <T : Any> Single<T>.execute(
            init: SingleObserverBuilder<T>.() -> Unit = {}
    ): Disposable =
            SingleObserverBuilder<T>()
                    .also(init)
                    .create()
                    .also { execute(it) }

    protected fun <T : Any> Single<T>.execute(o: BaseSingleObserver<T>) {
        subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o)
    }

    protected fun <T : Any> Observable<T>.execute(
            init: ObserverBuilder<T>.() -> Unit = {}
    ): Disposable =
            ObserverBuilder<T>().also(init).create().also { execute(it) }

    protected fun <T : Any> Observable<T>.execute(o: BaseObserver<T>) {
        subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o)
    }

    protected fun Completable.execute(
            init: CompletableObserverBuilder.() -> Unit = {}
    ): Disposable =
            CompletableObserverBuilder().also(init).create().also { execute(it) }

    protected fun Completable.execute(o: BaseCompletableObserver) {
        subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o)
    }

    protected fun <T : Any> Maybe<T>.execute(
            init: MaybeObserverBuilder<T>.() -> Unit = {}
    ): Disposable =
            MaybeObserverBuilder<T>()
                    .also(init)
                    .create()
                    .also { execute(it) }

    protected fun <T : Any> Maybe<T>.execute(o: BaseMaybeObserver<T>) {
        subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o)
    }

    open class BaseObserverBuilder {

        /**
         * DO NOT SWITCH TO THE ProgressDialog!!!
         *
         * Two reasons:
         *
         * 1) The progress dialog fragment is unsafe for the initial loading:
         * IllegalStateException: FragmentManager is already executing transactions
         * will be thrown
         *
         * 2) Progress dialogs aren't applicable for the initial loading by design
         * principles: we don't have the content to be shadowed
         *
         * Use the progress dialog behavior in your concrete observers
         */
        var progressBehavior: ProgressBehavior = ProgressBehavior.PreLoader

        protected var tryToHandle: (Throwable) -> Boolean = { false }
            private set

        var errorsMessageIds: Map<KClass<out Throwable>, Int> = emptyMap()

        protected var exitAfterCompletion = false
        var toastAfterCompletion = 0

        protected var onStart: () -> Unit = {}
        protected var onError: () -> Unit = {}


        fun tryToHandle(block: (Throwable) -> Boolean) {
            tryToHandle = block
        }

        inline fun <reified E : Throwable> errorToMessageId(messageId: Int) {
            errorsMessageIds += E::class to messageId
        }

        fun onStart(block: () -> Unit) {
            onStart = block
        }

        fun onError(block: () -> Unit) {
            onError = block
        }

        fun exitAfterCompletion(toastAfterCompletion: Int = 0) {
            exitAfterCompletion = true
            this.toastAfterCompletion = toastAfterCompletion
        }
    }


    protected inner class SingleObserverBuilder<T> : BaseObserverBuilder() {

        private var onSuccess: (T) -> Unit = {}

        fun onSuccess(block: (T) -> Unit) {
            onSuccess = block
        }

        fun create(): BaseSingleObserver<T> = SingleObserverImpl()


        private inner class SingleObserverImpl : BaseSingleObserver<T>(
                progressBehavior,
                exitAfterCompletion,
                toastAfterCompletion
        ) {

            override fun tryToHandle(e: Throwable) =
                    this@SingleObserverBuilder.tryToHandle(e)

            override fun toMessageId(e: Throwable) =
                    this@SingleObserverBuilder.errorsMessageIds[e::class]
                            ?: super.toMessageId(e)

            override fun onStart() {
                super.onStart()
                this@SingleObserverBuilder.onStart()
            }

            override fun onSuccess(t: T) {
                super.onSuccess(t)
                this@SingleObserverBuilder.onSuccess(t)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                this@SingleObserverBuilder.onError()
            }
        }
    }


    protected inner class ObserverBuilder<T> : BaseObserverBuilder() {

        private var onNext: (T) -> Unit = {}
        private var onComplete: () -> Unit = {}

        init {
            progressBehavior = ProgressBehavior.Empty
        }

        fun onNext(block: (T) -> Unit) {
            onNext = block
        }

        fun onComplete(block: () -> Unit) {
            onComplete = block
        }

        fun create(): BaseObserver<T> = ObserverImpl()


        private inner class ObserverImpl : BaseObserver<T>(
                progressBehavior,
                exitAfterCompletion,
                toastAfterCompletion
        ) {

            override fun tryToHandle(e: Throwable) =
                    this@ObserverBuilder.tryToHandle(e)

            override fun toMessageId(e: Throwable) =
                    this@ObserverBuilder.errorsMessageIds[e::class]
                            ?: super.toMessageId(e)

            override fun onStart() {
                super.onStart()
                this@ObserverBuilder.onStart()
            }

            override fun onNext(t: T) {
                this@ObserverBuilder.onNext(t)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                this@ObserverBuilder.onError()
            }

            override fun onComplete() {
                super.onComplete()
                this@ObserverBuilder.onComplete()
            }
        }
    }


    protected inner class CompletableObserverBuilder : BaseObserverBuilder() {

        private var onComplete: () -> Unit = {}

        fun onComplete(block: () -> Unit) {
            onComplete = block
        }

        fun create(): BaseCompletableObserver = CompletableObserverImpl()


        private inner class CompletableObserverImpl : BaseCompletableObserver(
                progressBehavior,
                exitAfterCompletion,
                toastAfterCompletion
        ) {

            override fun tryToHandle(e: Throwable) =
                    this@CompletableObserverBuilder.tryToHandle(e)

            override fun toMessageId(e: Throwable) =
                    this@CompletableObserverBuilder.errorsMessageIds[e::class]
                            ?: super.toMessageId(e)

            override fun onStart() {
                super.onStart()
                this@CompletableObserverBuilder.onStart()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                this@CompletableObserverBuilder.onError()
            }

            override fun onComplete() {
                super.onComplete()
                this@CompletableObserverBuilder.onComplete()
            }
        }
    }


    protected inner class MaybeObserverBuilder<T> : BaseObserverBuilder() {

        private var onComplete: () -> Unit = {}
        private var onSuccess: (T) -> Unit = {}

        fun onComplete(block: () -> Unit) {
            onComplete = block
        }

        fun onSuccess(block: (T) -> Unit) {
            onSuccess = block
        }

        fun create(): BaseMaybeObserver<T> = MaybeObserverImpl()


        private inner class MaybeObserverImpl : BaseMaybeObserver<T>(
                progressBehavior,
                exitAfterCompletion,
                toastAfterCompletion
        ) {

            override fun tryToHandle(e: Throwable) =
                    this@MaybeObserverBuilder.tryToHandle(e)

            override fun toMessageId(e: Throwable) =
                    this@MaybeObserverBuilder.errorsMessageIds[e::class]
                            ?: super.toMessageId(e)

            override fun onStart() {
                super.onStart()
                this@MaybeObserverBuilder.onStart()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                this@MaybeObserverBuilder.onError()
            }

            override fun onSuccess(t: T) {
                super.onSuccess(t)
                this@MaybeObserverBuilder.onSuccess(t)
            }
            override fun onComplete() {
                super.onComplete()
                this@MaybeObserverBuilder.onComplete()
            }
        }
    }


    private interface ObserverBehavior : Disposable {

        val owner: BaseViewModel

        val progressBehavior: ProgressBehavior

        val toastAfterCompletion get() = 0
        val exitAfterCompletion get() = false

        private val router get() = owner.router
        private val toastManager get() = owner.toastManager

        fun toMessageId(e: Throwable) =
                when (e) {
                    is ConnectException,
                    is SocketTimeoutException,
                    is UnknownHostException -> R.string.error_connection
                    else -> 0
                }

        /**
         * if true alert will not be shown
         */
        fun tryToHandle(e: Throwable) = false

        /* not overridable */
        fun ObserverBehavior.showAlertIfNotHandled(e: Throwable) {
            if (!tryToHandle(e)) {

                val messageId = toMessageId(e)

                val (screenKey, data) = when (messageId) {
                    0 -> Screens.ERROR to e
                    else -> Screens.ALERT to messageId
                }

                router.navigateTo(screenKey, data)
            }
        }

        /* not overridable */
        fun ObserverBehavior.processStart() {
            progressBehavior.processStart(owner, this)
            owner.disposables.add(this)
        }

        /* not overridable */
        fun ObserverBehavior.processError(e: Throwable) {
            progressBehavior.processTerminate(owner, this)
            showAlertIfNotHandled(e)
        }

        /* not overridable */
        fun ObserverBehavior.processCompletion() {
            progressBehavior.processTerminate(owner, this)

            if (toastAfterCompletion != 0) {
                toastManager.showToast(toastAfterCompletion)
            }

            if (exitAfterCompletion) {
                router.exit()
            }
        }
    }


    sealed class ProgressBehavior(
            val processStart: (BaseViewModel, Disposable) -> Unit,
            val processTerminate: (BaseViewModel, Disposable) -> Unit
    ) {

        object PreLoader : ProgressBehavior(
                processStart = { viewModel, _ -> viewModel.loadings.inc() },
                processTerminate = { viewModel, _ -> viewModel.loadings.dec() }
        )


        object ProgressDialog : BaseProgressDialog(cancelable = false)
        object CancelableProgressDialog : BaseProgressDialog(cancelable = true)

        object Empty : ProgressBehavior(
                processStart = { _, _ -> },
                processTerminate = { _, _ -> }
        )


        object Refresher : ProgressBehavior(
                { _, _ -> },
                { viewModel, _ -> (viewModel as WithRefresh).refreshable.notifyRefreshTerminated() }
        )


        class Custom(
                processStart: () -> Unit,
                processTerminate: () -> Unit
        ) : ProgressBehavior(
                { _, _ -> processStart() },
                { _, _ -> processTerminate() }
        )


        open class BaseProgressDialog(cancelable: Boolean = false) : ProgressBehavior(
                processStart = { viewModel, d ->
                    viewModel.router.navigateTo(
                            Screens.CANCELABLE_PROGRESS.takeIf { cancelable } ?: Screens.PROGRESS,
                            d
                    )
                },
                processTerminate = { viewModel, _ -> viewModel.router.exit() }
        )
    }

    protected fun ObservableBoolean.asProgress() = ProgressBehavior.Custom({ set(true) }, { set(false) })


    protected abstract inner class BaseObserver<T>(
            override val progressBehavior: ProgressBehavior,
            override val exitAfterCompletion: Boolean = false,
            override val toastAfterCompletion: Int = 0
    ) : DisposableObserver<T>(),
            ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onComplete() {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }


    protected abstract inner class BaseSingleObserver<T>(
            override val progressBehavior: ProgressBehavior,
            override val exitAfterCompletion: Boolean = false,
            override val toastAfterCompletion: Int = 0
    ) : DisposableSingleObserver<T>(),
            ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onSuccess(t: T) {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }


    protected open inner class BaseMaybeObserver<T>(
            override val progressBehavior: ProgressBehavior,
            override val exitAfterCompletion: Boolean,
            override val toastAfterCompletion: Int
    ) : DisposableMaybeObserver<T>(),
            ObserverBehavior {

        override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onSuccess(t: T) {
            processCompletion()
        }

        @CallSuper
        override fun onComplete() {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }


    protected abstract inner class BaseCompletableObserver(
            override val progressBehavior: ProgressBehavior,
            override val exitAfterCompletion: Boolean,
            override val toastAfterCompletion: Int
    ) : DisposableCompletableObserver(),
            ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onComplete() {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }


    protected inner class StorageProperty<T>(
            private val property: KMutableProperty1<LocalStorage, T>
    ) : ReadWriteProperty<LocalStorage, T> {

        override fun getValue(thisRef: LocalStorage, property: KProperty<*>): T =
                this.property.get(storage)

        override fun setValue(thisRef: LocalStorage, property: KProperty<*>, value: T) {
            this.property.set(storage, value)
        }
    }


    protected inner class LazyRefreshable<T : Any>(
            private val repo: LazyRepo<T>,
            private val onLoaded: (T) -> Unit,
            private val onFailed: () -> Unit
    ) : Refreshable() {

        private var criteria: ReloadCriteria = ReloadCriteria.IF_EXPIRED

        override fun onRefresh() {

            repo.get(criteria).execute {
                onNext(onLoaded)
                onError {
                    notifyRefreshTerminated()
                    onFailed()
                }
                onComplete(::notifyRefreshTerminated)
            }

            criteria = ReloadCriteria.FORCED
        }
    }


    interface ToastManager {
        fun showToast(message: Int)
    }
}
