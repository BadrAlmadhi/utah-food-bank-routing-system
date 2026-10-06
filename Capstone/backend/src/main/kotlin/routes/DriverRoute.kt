package com.utahfoodbank.routes

import com.utahfoodbank.database.addDriver
import com.utahfoodbank.database.deleteDriver
import com.utahfoodbank.database.getDrivers
import com.utahfoodbank.database.updateDriver
import com.utahfoodbank.model.Driver
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing


fun Application.configureDriverRoute() {
    routing {
        get("/drivers"){
            call.respond(getDrivers())
        }

        post("/drivers"){
            // react send to this
            val driver = call.receive<Driver>()
            addDriver(driver)
            call.respond(HttpStatusCode.Created)
        }

        delete("/drivers/{driverId}"){
            val driverId = call.parameters["driverId"]?.toIntOrNull()

            if (driverId == null) {
                call.respond(HttpStatusCode.BadRequest, "Driver Not Found")
            } else {
                deleteDriver(driverId)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        put("/drivers/{driverId}"){
            val driverId = call.parameters["driverId"]?.toIntOrNull()
            if (driverId == null) {
                call.respond(HttpStatusCode.BadRequest, "Driver Not Found")
            } else {

                val driver = call.receive<Driver>()
                updateDriver(driverId, driver)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}