package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Flat(
    val id: Int,
    val address: String,
    val price: Double,
    val details: String,
    val latitude: Double,
    val longitude: Double,
    val contactInfo: String,
    val amenities: MutableMap<String,Boolean>,
    val images: List<String>,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class Amenity(
    val map : MutableMap<String,Boolean>)

@Serializable
data class Images(val urls: List<String>)