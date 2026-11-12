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


// Reorder the delivery stops for a specific route.
//
// orderedDeliveryIds contains delivery IDs in the
// desired order, not client IDs or stop numbers.
fun reorderDeliveries(
    routeId: Int,
    orderedDeliveryIds: List<Int>
) {

    // Run all changes inside one database transaction.
    // If anything fails, Ktorm rolls back the changes.
    database.useTransaction {

        // Retrieve the deliveries belonging to this route
        val existingDeliveries = getDeliveriesByRoute(routeId)

        // Verify the request includes every delivery exactly once
        val existingIds = existingDeliveries.map { it.deliveryId }.toSet()
        val requestedIds = orderedDeliveryIds.toSet()

        require(
            orderedDeliveryIds.size == existingDeliveries.size &&
                    requestedIds.size == orderedDeliveryIds.size &&
                    requestedIds == existingIds
        ) {
            "The reordered list must contain every delivery exactly once"
        }

        // Our existing application uses positive stop numbers.
        // Ensure negative values are available for temporary use.
        require(existingDeliveries.all { it.stopOrder > 0 }) {
            "Existing stop numbers must be positive"
        }

        // PHASE 1:
        // Move every stop to a temporary negative number.
        //
        // Example: 1, 2, 3 becomes -1, -2, -3.
        // This avoids conflicts with existing positive stop orders.
        existingDeliveries.forEachIndexed { index, delivery ->

            val updatedRows = database.update(Deliveries) {

                set(Deliveries.stopOrder, -(index + 1))

                where {
                    it.id eq delivery.deliveryId
                }
            }

            check(updatedRows == 1) {
                "Failed to temporarily reorder delivery"
            }
        }

        // PHASE 2:
        // Assign the final positive stop numbers using
        // the optimized sequence from RouteOptimizer.
        orderedDeliveryIds.forEachIndexed { index, deliveryId ->

            val updatedRows = database.update(Deliveries) {

                // Stop numbers begin at 1
                set(Deliveries.stopOrder, index + 1)

                where {
                    it.id eq deliveryId
                }
            }

            check(updatedRows == 1) {
                "Failed to assign final stop order"
            }
        }
    }
}
