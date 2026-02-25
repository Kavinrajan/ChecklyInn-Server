package org.kvn.checklyinn.server.models

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TripDates : UUIDTable("TripDates") {
    val listingId = uuid("listing_id").references(TravelListings.id, onDelete = ReferenceOption.CASCADE)
    val startDate = timestamp("start_date")
    val endDate = timestamp("end_date")
    val maxCapacity = integer("max_capacity").nullable() // Overrides listing capacity if set
    val currentBookings = integer("current_bookings").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
}