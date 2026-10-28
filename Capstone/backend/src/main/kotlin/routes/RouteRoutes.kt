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
    }
}

