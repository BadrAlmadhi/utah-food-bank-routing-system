package com.utahfoodbank

import com.utahfoodbank.database.Clients
import com.utahfoodbank.database.addClient
import com.utahfoodbank.database.database
import com.utahfoodbank.database.getClients
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import com.utahfoodbank.model.Client
import org.ktorm.dsl.insert



val clients = mutableListOf<Client>(
    Client(
        clientId = 1,
        clientName = "Ahmed",
        clientAddress = "374 E Stonehedge DR",
        clientCity = "Salt Lake",
        clientZipCode = "84107"
    ),
    Client(
        clientId = 2,
        clientName = "Badr",
        clientAddress = "3782 E Stonehedge",
        clientCity = "Salt Lake",
        clientZipCode = "84107"
    )
)

fun Application.configureRouting() {


    routing {
        get("/") {
            call.respondText("Utah Food Bank Routing System")
        }

        get ("/clients") {
         call.respond(getClients())
        }

        post("/clients") {
            // kotlin objects
            // react send new client to this
            // then we add it to the client list
            val client = call.receive<Client>()

            // then add client
            addClient(client)
            call.respond(HttpStatusCode.Created)
        }

        delete("/clients/{clientId}") {

            val clientId = call.parameters["clientId"]?.toIntOrNull()

           if (clientId == null) {
               call.respond(HttpStatusCode.BadRequest, "User id not found")
           } else {
               clients.removeIf { it.clientId == clientId }
           }
        }

        put("/clients/{clientId}") {
            val clientId = call.parameters["clientId"]?.toIntOrNull()
            if (clientId == null) {
                call.respond(HttpStatusCode.BadRequest, "User id not found")
            } else {
                val updateClient = call.receive<Client>()
                val index = clients.indexOfFirst { it.clientId == clientId }

                if (index >= 0) {
                    clients[index] = updateClient.copy(clientId = clientId)
                    call.respond(HttpStatusCode.OK,clients[index])
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }
        }
    }
}