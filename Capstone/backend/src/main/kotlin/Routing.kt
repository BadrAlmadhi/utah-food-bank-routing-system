package com.utahfoodbank

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val clientId: Int,
    val clientName: String,
    val clientAddress: String,
    val clientCity: String,
    val clientZipCode: String
)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Utah Food Bank Routing System")
        }
        get ("/clients") {
            val clients = listOf(
                Client(
                    clientId = 1,
                    clientName = "Badr",
                    clientAddress = "374 E StoneHedge Dr",
                    clientCity = "Salt lake",
                    clientZipCode = "84107"
                ),
                Client(
                    clientId = 2,
                    clientName = "Charity London",
                    clientAddress = "3166 E Stone Hedge",
                    clientCity = "Salt lake",
                    clientZipCode = "84107"
                )
            )


            call.respond(clients)
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}