package com.utahfoodbank.routes

import com.utahfoodbank.database.addDelivery
import com.utahfoodbank.database.deleteDelivery
import com.utahfoodbank.database.getDeliveries
import com.utahfoodbank.database.getDeliveriesByRoute
import com.utahfoodbank.database.updateDelivery
import com.utahfoodbank.model.Delivery
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureDeliveryRoutes() {
    routing {

        // Get all deliveries
        get("/deliveries") {
            call.respond(getDeliveries())
        }

        // Get all delivery stops for a specific route
        get("/routes/{routeId}/deliveries") {

            // Read routeId from the URL
            val routeId = call.parameters["routeId"]?.toIntOrNull()

            // Check whether routeId is valid
            if (routeId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid route ID"
                )
                return@get
            }

            // Retrieve deliveries ordered by stopOrder
            val deliveries = getDeliveriesByRoute(routeId)

            // Send delivery list to React as JSON
            call.respond(deliveries)
        }

        post("/deliveries") {

            val delivery = call.receive<Delivery>()
            addDelivery(delivery)
            call.respond(HttpStatusCode.Created)

        }
        delete("/routes/{routeId}/deliveries/{deliveryId}") {

            // Get both IDs from the URL
            val routeId = call.parameters["routeId"]?.toIntOrNull()
            val deliveryId = call.parameters["deliveryId"]?.toIntOrNull()

            // Check both IDs
            if (routeId == null || deliveryId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid route or delivery ID"
                )
                return@delete
            }

            // Verify the delivery belongs to this route
            val deliveryExists = getDeliveriesByRoute(routeId)
                .any { it.deliveryId == deliveryId }

            if (!deliveryExists) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "Delivery not found in this route"
                )
                return@delete
            }

            // Delete using deliveryId, NOT routeId
            val deletedRow = deleteDelivery(deliveryId)

            if (deletedRow > 0) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Delivery not found")
            }
        }

        put("/routes/{routeId}/deliveries/{deliveryId}") {

            // Get route ID and delivery ID
            val routeId = call.parameters["routeId"]?.toIntOrNull()
            val deliveryId = call.parameters["deliveryId"]?.toIntOrNull()

            // Check both IDs
            if (routeId == null || deliveryId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid route or delivery ID"
                )
                return@put
            }

            // Receive updated delivery information
            val delivery = call.receive<Delivery>()

            // Make sure the delivery belongs to this route
            val deliveryExists = getDeliveriesByRoute(routeId)
                .any { it.deliveryId == deliveryId }

            if (!deliveryExists) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "Delivery not found in this route"
                )
                return@put
            }

            // Prevent changing the route through this endpoint
            if (delivery.routeId != routeId) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Delivery route ID does not match URL"
                )
                return@put
            }

            // Update delivery in SQLite
            val updatedRow = updateDelivery(deliveryId, delivery)

            if (updatedRow > 0) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Delivery not found")
            }
        }
    }
}



