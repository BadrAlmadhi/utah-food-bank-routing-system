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
                call.respond(HttpStatusCode.BadRequest, "Dispatcher Not Found")
            } else {
                deleteDispatcher(dispatcherId)
            }
        }

        // update
        put("/dispatchers/{dispatcherId}") {
            val dispatcherId = call.parameters["dispatcherId"]?.toIntOrNull()
            if (dispatcherId == null) {
                call.respond(HttpStatusCode.BadRequest, "Dispatcher Not Found")
            } else {
                val dispatcher = call.receive<Dispatcher>()
                updateDispatcher(dispatcherId, dispatcher)
            }
        }
    }
}

