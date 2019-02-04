package com.vsevolod.core

import com.vsevolod.core.gateways.GatewaysEngine
import com.vsevolod.core.gateways.LocalStorage
import com.vsevolod.core.gateways.NetworkFacade
import io.reactivex.Observable
import io.reactivex.Single
import kotlin.reflect.KMutableProperty1

typealias StorageExpirableProperty<T> = KMutableProperty1<LocalStorage, LocalStorage.ExpirableValue<T>>

enum class ReloadCriteria { IF_EXPIRED, FORCED }


sealed class LazyRepo<T : Any>(
        private val cache: StorageExpirableProperty<T>,
        private val update: (NetworkFacade) -> Single<T>
) : GatewaysEngine() {

    fun get(criteria: ReloadCriteria): Observable<T> =
            cache.get(localStorage).let { cached ->
                when {
                    criteria == ReloadCriteria.FORCED || cached.expired ->
                        update(networkFacade)
                                .doOnSuccess { cache.set(localStorage, LocalStorage.ExpirableValue(it)) }
                                .toObservable()
                                .let { observable ->
                                    when {
                                        cached.value == null -> observable
                                        else -> observable.startWith(cached.value)
                                    }
                                }

                    else -> Observable.just(cached.value)
                }
            }
}
