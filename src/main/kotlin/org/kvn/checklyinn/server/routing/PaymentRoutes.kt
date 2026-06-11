package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.services.BookingService
import org.kvn.checklyinn.server.services.PaymentService

fun Route.paymentRoutes(paymentService: PaymentService, bookingService: BookingService) {
    route("/payments") {
        authenticate("auth-jwt") {
            post("/intent") {
                // Implement payment intent creation logic here
            }

            post("/refund") {
                // Implement refund logic here
            }

            get("/intent/{paymentIntentId}") {
                val principal = call.principal<JWTPrincipal>()
            }
        }
    }
}