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


fun Application.configureRouting() {


    routing {
        get("/") {
            call.respondText("Utah Food Bank Routing System")
        }

        // read
        get ("/clients") {
         call.respond(getClients())
        }

        // add
        post("/clients") {
            // kotlin objects
            // react send new client to this
            // then we add it to the client list
            val client = call.receive<Client>()

            // then add client
            addClient(client)
            call.respond(HttpStatusCode.Created)
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