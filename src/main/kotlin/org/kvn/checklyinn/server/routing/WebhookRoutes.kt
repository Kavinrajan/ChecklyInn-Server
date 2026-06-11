package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.services.BookingService
import org.kvn.checklyinn.server.services.PaymentService

fun Route.webhookRoutes(paymentService: PaymentService, bookingService: BookingService) {
    route("/webhooks") {
        post("/stripe") {
            // Implement Stripe webhook handling logic here
        }

    }
}