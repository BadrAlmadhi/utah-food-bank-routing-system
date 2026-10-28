package com.utahfoodbank.model

import kotlinx.serialization.Serializable


@Serializable
data class Driver (
    val driverName: String,
    val driverEmail: String,
    val driverPhoneNumber: String,
    val vanNumber: String,
    val driverId: Int = 0 // we do this because it's not saved yet after saved assign id
)