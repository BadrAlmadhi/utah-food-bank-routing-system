package com.utahfoodbank.database

import com.utahfoodbank.model.Route
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
import java.lang.ScopedValue.where
import java.lang.reflect.Array.set


object Routes : Table<Nothing>("routes") {
    val id = int("routeId").primaryKey()
    val routeName = varchar("routeName")
    val driverId = int("driverId")
    val deliveryDate = varchar("deliveryDate")
}

fun getRoutes(): List<Route> {
    return database
        .from(Routes)
        .select()
        .map { row ->
            Route(
                routeId = row[Routes.id]!!,
                routeName = row[Routes.routeName]!!,
                driverId = row[Routes.driverId]!!,
                deliveryDate = row[Routes.deliveryDate]!!
            )
        }
}


fun addRoute(route: Route) {
    database.insert(Routes) {
        set(Routes.routeName, route.routeName)
        set(Routes.driverId, route.driverId)
        set(Routes.deliveryDate, route.deliveryDate)
    }
}

fun deleteRoute(routeId: Int): Int {
    return database.delete(Routes) {
        it.id eq routeId
    }
}

fun updateRoute(routeId: Int, route: Route): Int {
    return database.update(Routes) {
        set(Routes.routeName, route.routeName)
        set(Routes.driverId, route.driverId)
        set(Routes.deliveryDate, route.deliveryDate)

        where {
            it.id eq routeId
        }
    }
}