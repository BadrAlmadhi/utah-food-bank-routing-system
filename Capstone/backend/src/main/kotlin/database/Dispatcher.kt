package com.utahfoodbank.database

import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import com.utahfoodbank.model.Dispatcher
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update


object Dispatchers : Table<Nothing>("dispatchers") {
    val id = int("dispatcherId").primaryKey()
    val dispatcherName = varchar("dispatcherName")
    val dispatcherEmail = varchar("dispatcherEmail")
    val dispatcherPhoneNumber = varchar("dispatcherPhoneNumber")
}


// get
fun getDispatcher() : List<Dispatcher> {
    return database
        .from(Dispatchers)
        .select()
        .map { row ->
            Dispatcher(
                dispatcherId = row[Dispatchers.id]!!,
                dispatcherName = row[Dispatchers.dispatcherName]!!,
                dispatcherEmail = row[Dispatchers.dispatcherEmail]!!,
                dispatcherPhoneNumber = row[Dispatchers.dispatcherPhoneNumber]!!

            )
        }
}

// Insert
fun addDispatcher(dispatcher: Dispatcher){
    database.insert(Dispatchers) {
        set(Dispatchers.id, dispatcher.dispatcherId)
        set(Dispatchers.dispatcherName, dispatcher.dispatcherName)
        set(Dispatchers.dispatcherEmail, dispatcher.dispatcherEmail)
        set(Dispatchers.dispatcherPhoneNumber, dispatcher.dispatcherPhoneNumber)
    }
}

// delete
fun deleteDispatcher(dispatcherId: Int){
    database.delete(Dispatchers) {
        it.id eq dispatcherId
    }
}

// update
fun updateDispatcher(dispatcherId: Int, dispatcher: Dispatcher){
    database.update(Dispatchers) {
        set(Dispatchers.dispatcherName, dispatcher.dispatcherName)
        set(Dispatchers.dispatcherEmail, dispatcher.dispatcherEmail)
        set(Dispatchers.dispatcherPhoneNumber, dispatcher.dispatcherPhoneNumber)

        where {
            it.id eq dispatcherId
        }
    }
}
