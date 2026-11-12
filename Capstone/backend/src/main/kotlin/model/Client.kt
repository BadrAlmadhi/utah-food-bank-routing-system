package com.utahfoodbank.model

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val clientName: String,
    val clientAddress: String,
    val clientCity: String,
    val clientZipCode: String,

    // Geographic coordinates used for route optimization.
    // Nullable because existing clients don't have coordinates yet.
    val latitude: Double? = null,
    val longitude: Double? = null,

    // SQLite automatically generates the client ID.
    val clientId: Int = 0
)