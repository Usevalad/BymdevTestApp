package com.vsevolod.data

import com.vsevolod.core.entities.User
import com.vsevolod.core.gateways.NetworkFacade
import com.vsevolod.data.internal.Api
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkFacadeImpl @Inject constructor(private val api: Api) : NetworkFacade {
    override fun users(): Single<List<User>> = unimplementedSingle("users")

    private fun unimplementedCompletable(methodName: String) =
            Completable.error(unimplementedError(methodName))

    private fun <T> unimplementedSingle(methodName: String) =
            Single.error<T>(unimplementedError(methodName))

    private fun unimplementedError(methodName: String) =
            UnsupportedOperationException("$methodName() is not implemented")
}
