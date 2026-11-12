package com.utahfoodbank

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*

// Configure CORS so our React frontend
// can communicate with our Ktor backend.
fun Application.configureCors() {

    // Install the Cross-Origin Resource Sharing (CORS) plugin.
    // React runs on port 5173, while Ktor runs on port 8080.
    // Since they have different origins, the browser
    // requires permission before allowing certain requests.
    install(CORS) {

        // Allow requests coming from our React frontend.
        // This permits http://localhost:5173
        allowHost("localhost:5173", schemes = listOf("http"))

        // Allow GET requests.
        // Used to retrieve clients, drivers, routes, etc.
        allowMethod(HttpMethod.Get)

        // Allow POST requests.
        // Used to create new clients, drivers, and routes.
        allowMethod(HttpMethod.Post)

        // Allow PUT requests.
        // Used to update existing database records.
        allowMethod(HttpMethod.Put)

        // Allow DELETE requests.
        // Used to remove records from the database.
        allowMethod(HttpMethod.Delete)

        // Allow the Content-Type HTTP request header.
        // React uses "application/json" when sending JSON
        // to Ktor through POST and PUT requests.
        allowHeader(HttpHeaders.ContentType)
    }
}