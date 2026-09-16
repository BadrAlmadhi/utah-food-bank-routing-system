package com.utahfoodbank.model



import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val clientId: Int,
    val clientName: String,
    val clientAddress: String,
    val clientCity: String,
    val clientZipCode: String
)