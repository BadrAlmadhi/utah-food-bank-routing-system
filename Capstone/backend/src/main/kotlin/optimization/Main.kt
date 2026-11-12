import com.utahfoodbank.optimization.DeliveryLocation
import com.utahfoodbank.optimization.RouteOptimizer

fun main() {

    // Create three sample delivery locations
    val deliveries = listOf(
        DeliveryLocation(1, 40.70, -111.90),
        DeliveryLocation(2, 40.73, -111.91),
        DeliveryLocation(3, 40.72, -111.88)
    )

    // Optimize the delivery sequence
    val optimized = RouteOptimizer.optimize(
        startLatitude = 40.71,
        startLongitude = -111.90,
        deliveries = deliveries
    )

    // Print the optimized stop order
    optimized.forEachIndexed { index, delivery ->
        println(
            "Stop ${index + 1}: Delivery ${delivery.deliveryId}"
        )
    }
}
