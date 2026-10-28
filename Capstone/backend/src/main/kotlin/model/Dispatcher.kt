package com.utahfoodbank.model

import kotlinx.serialization.Serializable



@Serializable
data class Dispatcher(
    val dispatcherName: String,
    val dispatcherEmail: String,
    val dispatcherPhoneNumber: String,
    val dispatcherId: Int = 0
)