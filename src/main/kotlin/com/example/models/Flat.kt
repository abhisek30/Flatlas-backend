package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Flat(val id: Int, val address: String, val price: Double, val details: String, val latitude: Double, val longitude: Double)