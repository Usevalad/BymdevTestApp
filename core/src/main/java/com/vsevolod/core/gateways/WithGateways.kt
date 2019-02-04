package com.vsevolod.core.gateways

import javax.inject.Inject

interface WithGateways {
    val localStorage: LocalStorage
    val networkFacade: NetworkFacade
}


open class GatewaysEngine @Inject constructor() : WithGateways {
    @Inject override lateinit var localStorage: LocalStorage
    @Inject override lateinit var networkFacade: NetworkFacade
}
