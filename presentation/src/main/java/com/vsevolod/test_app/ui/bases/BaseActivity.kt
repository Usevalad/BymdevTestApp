package com.vsevolod.test_app.ui.bases

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.vsevolod.core.gateways.LocalStorage
import com.vsevolod.test_app.BuildConfig
import com.vsevolod.test_app.R
import com.vsevolod.test_app.ui.AppEvents
import com.vsevolod.test_app.ui.Screens
import com.vsevolod.test_app.ui.bases.pickers.DatePicker
import com.vsevolod.test_app.ui.screens.MainActivity
import com.vsevolod.test_app.ui.utils.screenKey
import com.vsevolod.test_app.ui.utils.setViewModel
import com.vsevolod.test_app.ui.utils.showSimpleAlert
import com.vsevolod.test_app.ui.utils.transitionData
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.custom.customView
import org.joda.time.LocalDate
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), AppEvents.Listener {

    @Inject lateinit var router: Router
    @Inject lateinit var navigatorHolder: NavigatorHolder

    @Inject lateinit var storage: LocalStorage
    @Inject lateinit var events: AppEvents

    protected open val layoutId = 0
    protected open val navigator: Navigator = BaseNavigator()
    protected open val setupToolbarAction: SetupToolbarAction? = null

    private var alert: DialogInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        events.addListener(this)
        layoutId.takeIf { it != 0 }?.let(::setContentView)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupToolbarAction?.perform()
    }

    override fun onDestroy() {
        super.onDestroy()
        events.removeListener(this)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        tryToFind<Fragment>().let { fragment ->
            if (fragment !is BaseFragment || !fragment.onBackPressed()) {
                router.exit()
            }
        }
    }

    protected open fun onNavCommandApplied(command: Command) {}

    protected open fun currentFragment(): Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

    protected inline fun <reified F> currentFragment(): F = currentFragment() as F

    protected inline fun <reified F : Fragment> tryToFind(): F? =
            supportFragmentManager.fragments.find { it is F } as? F

    protected inline fun <reified F : Fragment> find(): F = tryToFind()!!

    protected fun hideKeyboard() {
        val fragmentRoot = currentFragment()
                ?.view
                ?.rootView

        (fragmentRoot ?: currentFocus)
                ?.let { inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    protected fun showKeyboard(editText: EditText) {
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun AlertBuilder<*>.replace() {
        alert?.dismiss()
        alert = show()
    }

    protected fun DialogFragment.show() {
        show(supportFragmentManager, javaClass.name)
    }

    protected inline fun <reified D : DialogFragment> dismissIfShown() =
            tryToFind<D>()?.dismiss()


    protected open inner class BaseNavigator : SupportAppNavigator(this, R.id.fragment_container) {

        override fun applyCommand(command: Command) {
            if (command is Back) {

                dismissIfShown<DialogFragment>() ?: super.applyCommand(command)

            } else {

                when (command.screenKey()) {

                    Screens.ALERT -> command.transitionData<Any>().let { data ->
                        when (data) {
                            is Int -> showSimpleAlert(data)
                            is String -> showSimpleAlert(data)
                            else -> throw IllegalAccessException()
                        }
                    }

                    Screens.ERROR -> showSimpleAlert(
                            command.transitionData<Throwable>().message
                                    ?: getString(R.string.error_unknown)
                    )

                    Screens.PROGRESS,
                    Screens.CANCELABLE_PROGRESS -> showProgress(
                            disposable = command.transitionData() as Disposable,
                            cancelable = command.screenKey() == Screens.CANCELABLE_PROGRESS
                    )

                    Screens.DATE_PICKER -> {

                        val viewModel = command.transitionData<DatePicker>()
                        val now = LocalDate.now()

                        DatePickerDialog(
                                this@BaseActivity,
                                DatePickerDialog.OnDateSetListener { _, y, m, d ->
                                    viewModel.notifyDatePicked(LocalDate(y, m + 1, d))
                                },
                                now.year,
                                now.monthOfYear - 1,
                                now.dayOfMonth
                        ).apply {
                            viewModel.maxDate?.toDateTimeAtStartOfDay()?.millis?.let(datePicker::setMaxDate)
                            viewModel.minDate?.toDateTimeAtStartOfDay()?.millis?.let(datePicker::setMinDate)
                        }.show()
                    }

                    else -> super.applyCommand(command)
                }
            }

            onNavCommandApplied(command)
        }

        override fun createActivityIntent(context: Context, screenKey: String, data: Any?) =
                when (screenKey) {
                    Screens.MAIN -> intentFor<MainActivity>()
                    else -> null
                }

        override fun createFragment(screenKey: String, data: Any?): Fragment? = null

        override fun unknownScreen(command: Command) {
            if (BuildConfig.DEBUG) {
                alert("Can't create a screen for passed screenKey: ${command.screenKey()}")
                        .show()
            }
        }
    }


    inner class SetupToolbarAction(
            private val titleRes: Int = R.string.app_name,
            private val onNavClick: (() -> Unit)? = null
    ) {

        fun perform() {

            val content = find<ViewGroup>(android.R.id.content)
            val contentView = contentView
            content.removeView(contentView)

            verticalLayout {

                customView<AppBarLayout> {

                    lparams(width = matchParent)

                    themedToolbar(R.style.AppTheme_Toolbar) {
                        backgroundResource = R.color.colorPrimary
                        navigationIcon = resolveUpIcon(context.theme)

                        setSupportActionBar(this@themedToolbar)
                        this@BaseActivity.setTitle(titleRes)
                        setNavigationOnClickListener { onNavClick?.invoke() ?: router.exit() }
                    }
                }

                addView(contentView)

            }.let(::setContentView)
        }

        private fun resolveUpIcon(theme: Resources.Theme): Drawable {

            val attrs = intArrayOf(android.R.attr.homeAsUpIndicator)
            val typedArray = theme.obtainStyledAttributes(0, attrs)

            val icon = typedArray.getDrawable(0)

            typedArray.recycle()
            return icon
        }
    }
}


abstract class BindingActivity<VDB : ViewDataBinding> : BaseActivity() {

    protected val binding by lazy { DataBindingUtil.bind<VDB>(contentView!!)!! }

    protected open val viewModel: Any get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setViewModel(viewModel)
    }
}
