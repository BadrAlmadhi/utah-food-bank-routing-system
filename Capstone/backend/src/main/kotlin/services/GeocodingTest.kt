import com.utahfoodbank.services.GeocodingService

fun main() {

    // Public example address from the U.S. Census Bureau
    val address = "4600 Silver Hill Rd, Washington, DC 20233"

    try {
        // Ask the geocoding service for coordinates
        val coordinates = GeocodingService.geocode(address)

        if (coordinates != null) {
            println("Latitude: ${coordinates.latitude}")
            println("Longitude: ${coordinates.longitude}")
        } else {
            println("Address not found")
        }

    } catch (e: Exception) {
        println("Geocoding failed: ${e.message}")
    }
}
