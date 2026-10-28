package com.utahfoodbank.model

import kotlinx.serialization.Serializable

@Serializable
data class Delivery(
    val routeId: Int,
    val clientId: Int,
    val stopOrder: Int,
    val status: String = "Pending",
    val deliveryId: Int = 0
)
