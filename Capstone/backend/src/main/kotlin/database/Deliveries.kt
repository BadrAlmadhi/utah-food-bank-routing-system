package com.utahfoodbank.database

import com.utahfoodbank.model.Delivery
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.dsl.eq
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.asc
import org.ktorm.dsl.delete
import org.ktorm.dsl.insert
import org.ktorm.dsl.update

object Deliveries : Table<Nothing>("deliveries") {
    val id = int("deliveryId").primaryKey()
    val routeId = int("routeId")
    val clientId = int("clientId")
    val stopOrder = int("stopOrder")
    val status = varchar("status")
}

// get deliveries
fun getDeliveries(): List<Delivery> {
    return database
        .from(Deliveries)
        .select()
        .map { row ->
            Delivery(
                deliveryId = row[Deliveries.id]!!,
                routeId = row[Deliveries.routeId]!!,
                clientId = row[Deliveries.clientId]!!,
                stopOrder = row[Deliveries.stopOrder]!!,
                status = row[Deliveries.status]!!
            )
        }
}

fun getDeliveriesByRoute(routeId: Int): List<Delivery> {
    return database
        .from(Deliveries)
        .select()
        .where { Deliveries.routeId eq routeId }
        .orderBy(Deliveries.stopOrder.asc())
        .map { row ->
            Delivery(
                deliveryId = row[Deliveries.id]!!,
                routeId = row[Deliveries.routeId]!!,
                clientId = row[Deliveries.clientId]!!,
                stopOrder = row[Deliveries.stopOrder]!!,
                status = row[Deliveries.status]!!
            )
        }
}

fun addDelivery(delivery: Delivery) {
    database.insert(Deliveries) {
        set(Deliveries.routeId, delivery.routeId)
        set(Deliveries.clientId, delivery.clientId)
        set(Deliveries.stopOrder, delivery.stopOrder)
        set(Deliveries.status, delivery.status)
    }
}

fun deleteDelivery(deliveryId: Int): Int  {
    return database.delete(Deliveries) {
        it.id eq deliveryId
    }
}

fun updateDelivery(deliveryId: Int , delivery: Delivery): Int {
    return database.update(Deliveries) {
        set(it.routeId, delivery.routeId)
        set(it.clientId, delivery.clientId)
        set(it.stopOrder, delivery.stopOrder)
        set(it.status, delivery.status)

        where {
            it.id eq deliveryId
        }
    }
}