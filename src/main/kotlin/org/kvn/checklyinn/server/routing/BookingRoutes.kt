package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.services.BookingAvailabilityService
import org.kvn.checklyinn.server.services.BookingService

fun Route.bookingRoutes(
    bookingService: BookingService,
    availabilityService: BookingAvailabilityService
) {
    route("/bookings") {
        authenticate("auth-jwt") {
            // Check availability and calculate price
            post("check-availability") {
                try {

                } catch (e: Exception) {

                }
            }

            post {

            }

            get {

            }

            get("/{id}") {

            }

            put("/{id}/status") {

            }

            put("/{id}/payment") {

            }

            delete("/{id}") {

            }
        }
    }
}