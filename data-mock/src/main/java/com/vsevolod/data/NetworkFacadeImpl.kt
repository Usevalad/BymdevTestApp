package com.vsevolod.data

import com.vsevolod.core.entities.User
import com.vsevolod.core.gateways.NetworkFacade
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkFacadeImpl @Inject constructor() : NetworkFacade {

    override fun users(): Single<List<User>> = justDelayed(users, 2)

    private val users = listOf(
            User(firstName = "Lisa", lastName = "Monro", age = 23, id = 1, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "Henry", lastName = "Monro", age = 17, id = 2, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "Lola", lastName = "Monro", age = 23, id = 3, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "Ivor", lastName = "Monro", age = 7, id = 4, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "Nat", lastName = "Monro", age = 82, id = 5, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "John", lastName = "Monro", age = 66, id = 6, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "Dean", lastName = "Monro", age = 98, id = 7, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png"),
            User(firstName = "Albert", lastName = "Monro", age = 1, id = 8, avatarUrl = "https://www.stickees.com/files/avatars/male-avatars/1697-andrew-sticker.png")
    )

    private val random = Random()

    private fun <T> justDelayed(item: T, delaySeconds: Long = 2) =
            Observable.timer(delaySeconds, TimeUnit.SECONDS)
                    .firstOrError()
                    .map { item }

    private fun delayedCompletable(delaySeconds: Long = 2) =
            justDelayed(Unit, delaySeconds)
                    .toCompletable()
}
