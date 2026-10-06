package com.utahfoodbank.database


import com.utahfoodbank.model.Driver
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar


// create object
object Drivers : Table<Nothing>("drivers") {
    val id = int("driverId").primaryKey()
    val driverName = varchar("driverName")
    val driverEmail = varchar("driverEmail")
    val driverPhoneNumber = varchar("driverPhoneNumber")
    val vanNumber = varchar("vanNumber")
}


fun getDrivers(): List<Driver> {
    return database
        .from(Drivers)
        .select()
        .map { row ->
            Driver(
                driverId = row[Drivers.id]!!,
                driverName = row[Drivers.driverName]!!,
                driverEmail = row[Drivers.driverEmail]!!,
                driverPhoneNumber = row[Drivers.driverPhoneNumber]!!,
                vanNumber = row[Drivers.vanNumber]!!
            )
        }
}



// Insert
fun addDriver(driver: Driver) {
    database.insert(Drivers) {
        set(Drivers.id, driver.driverId)
        set(Drivers.driverName, driver.driverName)
        set(Drivers.driverEmail, driver.driverEmail)
        set(Drivers.driverPhoneNumber, driver.driverPhoneNumber)
        set(Drivers.vanNumber, driver.vanNumber)
    }
}

// delete
fun deleteDriver(driverId: Int) {
    database.delete(Drivers) {
        it.id eq driverId
    }
}

// update
fun updateDriver(driverId: Int, driver: Driver) {
    database.update(Drivers) {
        set(Drivers.driverName, driver.driverName)
        set(Drivers.driverEmail, driver.driverEmail)
        set(Drivers.driverPhoneNumber, driver.driverPhoneNumber)
        set(Drivers.vanNumber, driver.vanNumber)

        where {
            it.id eq driverId
        }
    }
}