package org.kvn.checklyinn.server.services

import org.kvn.checklyinn.server.models.TravelListings
import java.math.BigDecimal
import kotlinx.datetime.Instant
import java.util.UUID
import org.jetbrains.exposed.sql.*

class BookingService {

    suspend fun createBooking(
        ustomerId: String,
        listingId: String,
        checkInDate: String?,
        checkOutDate: String?,
        numberOfGuests: Int,
        specialRequests: String?,
        totalPrice: BigDecimal? = null // If provided, use this; otherwise calculate
    ) {
        // Get listing price
        val listing = TravelListings.select { TravelListings.id eq UUID.fromString(listingId) }.singleOrNull()
            ?: throw IllegalArgumentException("Listing not found")
        val calculatedPrice = totalPrice ?: run {
            val basePrice = listing[TravelListings.price]
            // For hotels, calculate based on nights
            val checkIn = checkInDate?.let { Instant.parse(it) }
            val checkOut = checkOutDate?.let { Instant.parse(it) }

            if (checkIn != null && checkOut != null && listing[TravelListings.category] == "HOTEL") {
                val nights = ((checkOut.toEpochMilliseconds() - checkIn.toEpochMilliseconds()) / (1000 * 60 * 60 * 24)).toInt()
                basePrice.multiply(BigDecimal.valueOf(nights.toLong())).multiply(BigDecimal.valueOf(numberOfGuests.toLong()))
            } else {
                basePrice.multiply(BigDecimal.valueOf(numberOfGuests.toLong()))
            }
        }

        val id = Bookings.insert {
            it[Bookings.customerId] = UUID.fromString(customerId)
            it[Bookings.listingId] = UUID.fromString(listingId)
            it[Bookings.checkInDate] = checkInDate?.let { Instant.parse(it) }
            it[Bookings.checkOutDate] = checkOutDate?.let { Instant.parse(it) }
            it[Bookings.numberOfGuests] = numberOfGuests
            it[Bookings.totalPrice] = calculatedPrice
            it[Bookings.currency] = listing[TravelListings.currency]
            it[Bookings.status] = BookingStatus.PENDING.name
            it[Bookings.paymentStatus] = PaymentStatus.PENDING.name
            it[Bookings.specialRequests] = specialRequests
        }[Bookings.id].value
    }
}