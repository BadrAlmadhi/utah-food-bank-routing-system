package com.utahfoodbank.model

import kotlinx.serialization.Serializable


@Serializable
data class Driver (
    val driverId: Int,
    val driverName: String,
    val driverEmail: String,
    val driverPhoneNumber: String,
    val vanNumber: String
)