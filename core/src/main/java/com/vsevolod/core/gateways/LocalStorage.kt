package com.vsevolod.core.gateways

interface LocalStorage {

    var onboardingShown: Boolean


    data class ExpirableValue<T : Any>(
            val value: T?,
            val expired: Boolean = true
    )
}
