package com.utahfoodbank.model

import kotlinx.serialization.Serializable



@Serializable
data class Dispatcher(
    val dispatcherId: Int,
    val dispatcherName: String,
    val dispatcherEmail: String,
    val dispatcherPhoneNumber: String
)