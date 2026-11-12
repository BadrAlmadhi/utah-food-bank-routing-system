
package com.utahfoodbank.services

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

import kotlinx.serialization.json.*

// Represent the latitude and longitude of an address
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

// Service responsible for converting addresses into coordinates
object GeocodingService {

    // Create an HTTP client to communicate with the Census API
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    // Convert a street address into latitude and longitude
    fun geocode(address: String): Coordinates? {

        // Encode spaces and special characters for a URL
        val encodedAddress = URLEncoder.encode(
            address,
            StandardCharsets.UTF_8
        )

        // Census API endpoint
        val url =
            "https://geocoding.geo.census.gov/geocoder/locations/onelineaddress" +
                    "?address=$encodedAddress" +
                    "&benchmark=Public_AR_Current" +
                    "&format=json"

        // Build an HTTP GET request
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build()

        // Send the request and retrieve the JSON response
        val response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        // Check whether the API request succeeded
        if (response.statusCode() != 200) {
            throw IllegalStateException(
                "Geocoding API returned ${response.statusCode()}"
            )
        }

        // Convert the JSON response into Kotlin objects
        val json = Json.parseToJsonElement(response.body())

        // Find the first matching address
        val matches = json.jsonObject["result"]
            ?.jsonObject
            ?.get("addressMatches")
            ?.jsonArray

        // Return null if no address was found
        val firstMatch = matches?.firstOrNull() ?: return null

        // Extract the coordinates from the response
        val coordinates = firstMatch.jsonObject["coordinates"]
            ?.jsonObject ?: return null

        // Census uses y for latitude and x for longitude
        val latitude = coordinates["y"]?.jsonPrimitive?.doubleOrNull
            ?: return null

        val longitude = coordinates["x"]?.jsonPrimitive?.doubleOrNull
            ?: return null

        return Coordinates(
            latitude = latitude,
            longitude = longitude
        )
    }
}

