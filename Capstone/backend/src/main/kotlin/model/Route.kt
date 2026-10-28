package com.utahfoodbank.model

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val routeName: String,
    val driverId: Int,
    val deliveryDate: String,
    val routeId: Int = 0
)
