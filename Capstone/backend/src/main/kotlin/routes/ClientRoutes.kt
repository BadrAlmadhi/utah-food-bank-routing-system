package com.utahfoodbank.routes

import com.utahfoodbank.database.addClient
import com.utahfoodbank.database.deleteClient
import com.utahfoodbank.database.getClients
import com.utahfoodbank.database.updateClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import com.utahfoodbank.model.Client
import com.utahfoodbank.database.updateClientCoordinates
import com.utahfoodbank.services.GeocodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


fun Application.configureRouting() {


    routing {
        get("/") {
            call.respondText("Utah Food Bank Routing System")
        }

        // read
        get ("/clients") {
         call.respond(getClients())
        }

        // POST: Add a new client to the database
        post("/clients") {

            // Receive client information sent from React as JSON
            val client = call.receive<Client>()

            // Save the new client into SQLite
            addClient(client)

            // Return HTTP 201 to confirm the client was created
            call.respond(
                HttpStatusCode.Created,
                "Client added successfully"
            )
        }

        // add
        // POST: Convert a client's address into coordinates
        // Example: POST /clients/1/geocode
        post("/clients/{clientId}/geocode") {

            // Read the client ID from the URL
            val clientId = call.parameters["clientId"]?.toIntOrNull()

            if (clientId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid client ID"
                )
                return@post
            }

            // Find the client in our SQLite database
            val client = getClients().find {
                it.clientId == clientId
            }

            if (client == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "Client not found"
                )
                return@post
            }

            // Build the complete address
            val fullAddress = listOf(
                client.clientAddress,
                client.clientCity,
                "UT",
                client.clientZipCode
            ).joinToString(", ")

            try {
                // Call the Census API outside the Ktor event-loop thread.
                // This service uses Java's blocking HTTP client.
                val coordinates = withContext(Dispatchers.IO) {
                    GeocodingService.geocode(fullAddress)
                }

                // No matching geographic location was found
                if (coordinates == null) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        "No matching address found"
                    )
                    return@post
                }

                // Save latitude and longitude to SQLite
                val updated = updateClientCoordinates(
                    clientId = clientId,
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude
                )

                if (updated == 0) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        "Client no longer exists"
                    )
                    return@post
                }

                // Return the saved coordinates
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "clientId" to clientId.toString(),
                        "latitude" to coordinates.latitude.toString(),
                        "longitude" to coordinates.longitude.toString()
                    )
                )

            } catch (e: Exception) {
                // Log the technical error on the server
                call.application.environment.log.error(
                    "Geocoding failed for client ID $clientId",
                    e
                )

                call.respond(
                    HttpStatusCode.BadGateway,
                    "Geocoding service unavailable or failed"
                )
            }
        }


        // delete
        delete("/clients/{clientId}") {

            // URL parameter from string to int
            val clientId = call.parameters["clientId"]?.toIntOrNull()

            if (clientId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid client ID")
                return@delete
            }

            val deletedRows = deleteClient(clientId)
            if (deletedRows > 0 ){
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Client not found")
            }

        }

        // update
        put("/clients/{clientId}") {
            val clientId = call.parameters["clientId"]?.toIntOrNull()

            if (clientId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid client ID")
                return@put
            }

            val client = call.receive<Client>()
            val updatedRows = updateClient(clientId, client)
            if (updatedRows > 0 ){
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Client not found")
            }
        }
    }
}

// here we are managing HTTP requests 200 = ok
// 201 = created
// 204 No content
// 404 = Not found
// 400 = bad request