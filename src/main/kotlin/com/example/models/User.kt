package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val fcmToken: String = "",
    val passwordHash: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)