package com.vsevolod.test_app.ui.bases

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.vsevolod.core.gateways.LocalStorage
import com.vsevolod.test_app.R
import com.vsevolod.test_app.databinding.PreloaderBinding
import com.vsevolod.test_app.databinding.SwipeRefreshLayoutBinding
import com.vsevolod.test_app.ui.AppEvents
import com.vsevolod.test_app.ui.utils.disposedComposite
import com.vsevolod.test_app.ui.utils.setViewModel
import com.vsevolod.test_app.ui.utils.withViewModel
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.support.v4.act
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseFragment : DaggerFragment(), AppEvents.Listener {

    abstract val layoutId: Int

    protected open val titleId = 0
    protected open val menuId = 0
    protected open val withUpButton = false

    @Inject lateinit var router: Router
    @Inject lateinit var events: AppEvents
    @Inject lateinit var storage: LocalStorage

    private var onCreateDisposables = disposedComposite
    private var onResumeDisposables = disposedComposite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        events.addListener(this)
        onCreateDisposables = CompositeDisposable()
        setHasOptionsMenu(menuId != 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        onCreateDisposables.dispose()
        events.removeListener(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View =
            inflater.inflate(layoutId, container, false)

    override fun onResume() {
        super.onResume()
        onResumeDisposables = CompositeDisposable()

        if (titleId != 0) {
            act.setTitle(titleId)
        }

        (act as AppCompatActivity).supportActionBar?.apply {
            setDisplayShowHomeEnabled(withUpButton)
            setDisplayHomeAsUpEnabled(withUpButton)
        }
    }

    override fun onPause() {
        super.onPause()
        onResumeDisposables.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(menuId, menu)
    }

    open fun onBackPressed(): Boolean = false

    protected fun <T> Observable<T>.subscribeOnCreate(onNext: (T) -> Unit) {
        onCreateDisposables.add(subscribe(onNext))
    }

    protected fun <T> Observable<T>.subscribeOnResume(onNext: (T) -> Unit) {
        onResumeDisposables.add(subscribe(onNext))
    }
}


abstract class BindingFragment : BaseFragment() {

    protected open val viewModel: Any get() = this

    private val _bindings = mutableSetOf<ViewDataBinding>()
    protected val bindings: Set<ViewDataBinding> get() = _bindings

    override fun onResume() {
        super.onResume()

        Observable.interval(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOnResume { _ -> _bindings.forEach { it.executePendingBindings() } }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = safeInflate<ViewDataBinding>(layoutId, inflater, container)
            .root

    override fun onDestroyView() {
        super.onDestroyView()

        _bindings.forEach {
            try {
                it.unbind()
                it.setViewModel(null)
            } catch (ignored: RuntimeException) {
                ignored.printStackTrace()
            }
        }

        _bindings.clear()
    }

    protected fun <VDB : ViewDataBinding> safeInflate(
            layoutId: Int,
            inflater: LayoutInflater,
            viewGroup: ViewGroup? = null
    ): VDB =
            DataBindingUtil.inflate<VDB>(inflater, layoutId, viewGroup, false)
                    .withViewModel(viewModel, lifecycleOwner = this)
                    .also { _bindings.add(it) }

    protected inline fun <reified VDB : ViewDataBinding> binding() =
            bindings.filterIsInstance<VDB>()
                    .first()

    protected inline fun <reified VDB : ViewDataBinding> withBinding(block: VDB.() -> Unit) {
        block(binding())
    }
}


abstract class ViewModelFragment<VM : BaseViewModel> : BindingFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    final override val viewModel by lazy(::obtainViewModel)

    private val useActivityScope = arguments?.getBoolean(ARG_USE_ACTIVITY_SCOPE) ?: false

    protected abstract val viewModelClass: Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: (viewModel as? WithRefresh)?.refresh()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View =
            super.onCreateView(inflater, container, savedInstanceState)
                    .let { view ->
                        when (viewModel) {
                            is WithRefresh -> view.wrapBy(
                                    inflater,
                                    R.layout.swipe_refresh_layout,
                                    SwipeRefreshLayoutBinding::swipeRefreshLayout
                            )
                            else -> view
                        }
                    }
                    .wrapBy(inflater, R.layout.preloader, PreloaderBinding::viewSwitcherContent)

    private fun <VDB : ViewDataBinding> View.wrapBy(
            inflater: LayoutInflater,
            layoutId: Int,
            getViewGroup: VDB.() -> ViewGroup
    ): View =
            safeInflate<VDB>(layoutId, inflater)
                    .apply {
                        getViewGroup(this).addView(this@wrapBy)
                    }
                    .root

    private fun obtainViewModel() =
            when {
                useActivityScope -> ViewModelProviders.of(act as FragmentActivity, viewModelFactory)
                else -> ViewModelProviders.of(this, viewModelFactory)
            }.get(viewModelClass)

    override fun onBackPressed() = viewModel.onBackPressed()


    companion object {
        const val ARG_USE_ACTIVITY_SCOPE = "ARG_USE_ACTIVITY_SCOPE"
    }
}
