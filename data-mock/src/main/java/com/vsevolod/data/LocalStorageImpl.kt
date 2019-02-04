package com.vsevolod.data

import com.vsevolod.core.gateways.LocalStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageImpl @Inject constructor() : LocalStorage {
    override var onboardingShown = true
}
