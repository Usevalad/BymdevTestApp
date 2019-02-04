package com.vsevolod.core.entities

import java.io.Serializable

data class User constructor(
        val id: Long = 0,
        val firstName: String = "",
        val lastName: String = "",
        val avatarUrl: String = "",
        val birthDate: String = "",
        val age: Int = 0,
        val gender: String = "",
        val location: String = "",
        val email: String = ""
) : Serializable