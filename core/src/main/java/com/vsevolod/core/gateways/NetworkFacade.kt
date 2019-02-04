package com.vsevolod.core.gateways

import com.vsevolod.core.entities.User
import io.reactivex.Single

interface NetworkFacade {
    fun users(): Single<List<User>>
}
