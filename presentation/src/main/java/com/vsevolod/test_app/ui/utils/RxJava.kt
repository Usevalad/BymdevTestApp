package com.vsevolod.test_app.ui.utils

import io.reactivex.disposables.CompositeDisposable

val disposedComposite = CompositeDisposable().apply { dispose() }
