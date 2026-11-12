
package com.utahfoodbank.model

import kotlinx.serialization.Serializable

// Starting location supplied by the dispatcher
@Serializable
data class OptimizeRouteRequest(
    val startLatitude: Double,
    val startLongitude: Double
)

// Information returned after optimizing a route
@Serializable
data class OptimizeRouteResponse(
    val routeId: Int,
    val optimizedDeliveryIds: List<Int>,
    val totalStops: Int,
    val message: String
)

