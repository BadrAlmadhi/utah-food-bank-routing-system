package com.utahfoodbank.database

// describe the clients tables to ktorm

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import com.utahfoodbank.model.Client
import org.ktorm.dsl.insert
import org.ktorm.dsl.map

// make one kotlin object representing SQLite
// then table<nothing> = table who's actual name is clients
object Clients : Table<Nothing>("clients") {
    // ("Inside bracket must match the column name ")
    val id = int("client_id").primaryKey()
    val clientName = varchar("client_name")
    val clientAddress = varchar("client_address")
    val clientCity = varchar("client_city")
    val clientZipCode = varchar("client_zip_code")
}

// SELECT * FROM Clients
// : List<Client> return a list of clients
fun getClients() : List<Client> {
    return database
        .from(Clients)
        .select()
        .map { row ->
            Client(
                clientId = row[Clients.id]!!, // !! mean not NULL
                clientName = row[Clients.clientName]!!,
                clientAddress = row[Clients.clientAddress]!!,
                clientCity = row[Clients.clientCity]!!,
                clientZipCode = row[Clients.clientZipCode]!!
            )
        }
}

// INSERT INTO client ( .. ) VALUES ( .. )
fun addClient(client: Client) {
    database.insert(Clients) {
        set(Clients.clientName, client.clientName)
        set(Clients.clientAddress, client.clientAddress)
        set(Clients.clientCity, client.clientCity)
        set(Clients.clientZipCode, client.clientZipCode)
    }
}