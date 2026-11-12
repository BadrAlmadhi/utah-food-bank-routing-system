
package com.utahfoodbank.optimization

import kotlin.math.*

// Represents a delivery location with geographic coordinates
data class DeliveryLocation(
    val deliveryId: Int,
    val latitude: Double,
    val longitude: Double
)

// Responsible for calculating a suggested delivery sequence
object RouteOptimizer {

    // Calculate the distance between two geographic coordinates
    // using the Haversine formula.
    // The result is measured in kilometers.
    fun distanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        val earthRadiusKm = 6371.0

        // Convert degrees into radians
        val latitudeDifference = Math.toRadians(lat2 - lat1)
        val longitudeDifference = Math.toRadians(lon2 - lon1)

        val a =
            sin(latitudeDifference / 2).pow(2) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(longitudeDifference / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    // Arrange delivery locations using Nearest Neighbor
    fun optimize(
        startLatitude: Double,
        startLongitude: Double,
        deliveries: List<DeliveryLocation>
    ): List<DeliveryLocation> {

        // Copy the list so the original is not modified
        val remaining = deliveries.toMutableList()

        // Store the suggested delivery sequence
        val optimized = mutableListOf<DeliveryLocation>()

        // Begin at the provided starting location
        var currentLatitude = startLatitude
        var currentLongitude = startLongitude

        // Continue until every delivery has been visited
        while (remaining.isNotEmpty()) {

            // Find the closest remaining delivery
            val nearest = remaining.minWithOrNull(
                compareBy<DeliveryLocation> {
                    distanceKm(
                        currentLatitude,
                        currentLongitude,
                        it.latitude,
                        it.longitude
                    )
                }.thenBy { it.deliveryId }
            ) ?: break

            // Add that delivery to the optimized sequence
            optimized.add(nearest)

            // Remove it from unvisited deliveries
            remaining.remove(nearest)

            // Our next search starts from this delivery
            currentLatitude = nearest.latitude
            currentLongitude = nearest.longitude
        }

        return optimized
    }
}

