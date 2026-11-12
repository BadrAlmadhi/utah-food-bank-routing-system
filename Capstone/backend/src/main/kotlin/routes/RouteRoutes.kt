package com.utahfoodbank.routes


import com.utahfoodbank.database.addRoute
import com.utahfoodbank.database.deleteRoute
import com.utahfoodbank.database.getRoutes
import com.utahfoodbank.database.updateRoute
import com.utahfoodbank.model.Route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.utahfoodbank.database.getClients
import com.utahfoodbank.database.getDeliveriesByRoute
import com.utahfoodbank.database.reorderDeliveries
import com.utahfoodbank.optimization.DeliveryLocation
import com.utahfoodbank.optimization.RouteOptimizer
import com.utahfoodbank.model.OptimizeRouteRequest
import com.utahfoodbank.model.OptimizeRouteResponse

fun Application.configureRoutes() {

    routing {
        get("/routes") {
            call.respond(getRoutes())
        }

        post("/routes") {
            // react send route
            val route = call.receive<Route>()

            // add route
            addRoute(route)
            call.respond(HttpStatusCode.Created)
        }

        delete("/routes/{routeId}") {
            // get route id, and convert into int
            val routeId = call.parameters["routeId"]?.toIntOrNull()

            if (routeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid route ID")
                // stop delete
                return@delete
            }

            // call database function
            val deletedRows = deleteRoute(routeId)
            if (deletedRows > 0) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Route not found")
            }
        }

        put("/routes/{routeId}") {
            val routeId = call.parameters["routeId"]?.toIntOrNull()
            if (routeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid route ID")
                return@put
            }

            val route = call.receive<Route>()

            val updatedRows = updateRoute(routeId, route)
            if (updatedRows > 0) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Route not found")
            }
        }


// POST: Optimize an existing delivery route
        post("/routes/{routeId}/optimize") {

            // Get route ID from the request URL
            val routeId = call.parameters["routeId"]?.toIntOrNull()

            if (routeId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid route ID"
                )
                return@post
            }

            // Check whether the route exists
            val route = getRoutes().find { it.routeId == routeId }

            if (route == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "Route not found"
                )
                return@post
            }

            // Read the starting coordinates from the request body
            val request = call.receive<OptimizeRouteRequest>()

            // Check that the starting coordinates are valid
            if (
                !request.startLatitude.isFinite() ||
                !request.startLongitude.isFinite() ||
                request.startLatitude !in -90.0..90.0 ||
                request.startLongitude !in -180.0..180.0
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid starting coordinates"
                )
                return@post
            }

            // Retrieve deliveries assigned to this route
            val deliveries = getDeliveriesByRoute(routeId)

            if (deliveries.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "This route has no delivery stops"
                )
                return@post
            }

            // Retrieve client information, including coordinates
            val clientsById = getClients().associateBy { it.clientId }

            // Verify that every delivery has valid coordinates
            val locations = mutableListOf<DeliveryLocation>()

            for (delivery in deliveries) {
                val client = clientsById[delivery.clientId]

                val latitude = client?.latitude
                val longitude = client?.longitude

                if (
                    latitude == null ||
                    longitude == null ||
                    !latitude.isFinite() ||
                    !longitude.isFinite() ||
                    latitude !in -90.0..90.0 ||
                    longitude !in -180.0..180.0
                ) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        "Delivery ${delivery.deliveryId} has missing or invalid coordinates"
                    )
                    return@post
                }

                // Add this delivery's geographic location
                locations.add(
                    DeliveryLocation(
                        deliveryId = delivery.deliveryId,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }

            // Run the Nearest Neighbor optimization algorithm
            val optimized = RouteOptimizer.optimize(
                startLatitude = request.startLatitude,
                startLongitude = request.startLongitude,
                deliveries = locations
            )

            // Extract delivery IDs in the suggested order
            val optimizedIds = optimized.map { it.deliveryId }

            try {
                // Save the optimized delivery sequence
                reorderDeliveries(routeId, optimizedIds)

            } catch (e: IllegalArgumentException) {

                // The list failed validation
                call.respond(
                    HttpStatusCode.Conflict,
                    "Route deliveries changed during optimization"
                )
                return@post

            } catch (e: Exception) {

                // Log unexpected database failures
                call.application.environment.log.error(
                    "Failed to optimize route $routeId",
                    e
                )

                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to save optimized route"
                )
                return@post
            }

            // Return the optimized sequence
            call.respond(
                HttpStatusCode.OK,
                OptimizeRouteResponse(
                    routeId = routeId,
                    optimizedDeliveryIds = optimizedIds,
                    totalStops = optimizedIds.size,
                    message = "Route optimized successfully"
                )
            )
        }

    }
}

