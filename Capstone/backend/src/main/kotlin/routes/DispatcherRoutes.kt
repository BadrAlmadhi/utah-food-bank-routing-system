package com.utahfoodbank.routes

import com.utahfoodbank.database.addDispatcher
import com.utahfoodbank.database.deleteDispatcher
import com.utahfoodbank.database.getDispatcher
import com.utahfoodbank.database.updateDispatcher
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import com.utahfoodbank.model.Client
import com.utahfoodbank.model.Dispatcher


fun Application.configureDispatcherRoutes() {
    routing{

        // get/ read
        get("/dispatchers") {
            call.respond(getDispatcher())
        }


        // add
        post("/dispatchers") {
            val dispatcher = call.receive<Dispatcher>()
            addDispatcher(dispatcher)
            call.respond(HttpStatusCode.Created)
        }

        // delete
        delete("/dispatchers/{dispatcherId}") {
            val dispatcherId = call.parameters["dispatcherId"]?.toIntOrNull()

            if (dispatcherId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid dispatcher ID")
                return@delete
            }

            // delete dispatcher from database
            val deletedRow = deleteDispatcher(dispatcherId)

            // check weather a dispatcher was deleted
            if (deletedRow > 0) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Dispatcher not found")
            }
        }

        // update
        put("/dispatchers/{dispatcherId}") {
            val dispatcherId = call.parameters["dispatcherId"]?.toIntOrNull()
            if (dispatcherId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid dispatcher ID")
                return@put
            }

            // receive updated dispatcher information from react
            val dispatcher = call.receive<Dispatcher>()

            // update dispatcher in database
            val updatedRow = updateDispatcher(dispatcherId, dispatcher)

            // check weather the update succeeded
            if (updatedRow > 0) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Dispatcher not found")
            }
        }
    }
}

