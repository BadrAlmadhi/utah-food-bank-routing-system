package com.utahfoodbank.model



import kotlinx.serialization.Serializable


// one Client
@Serializable
data class Client(
    val clientName: String,
    val clientAddress: String,
    val clientCity: String,
    val clientZipCode: String,
    val clientId: Int = 0
)