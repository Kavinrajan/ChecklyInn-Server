package org.kvn.checklyinn.server

import org.kvn.checklyinn.server.services.ImageService

fun Application.configureRouting(imageService: ImageService) {
    val config = environment.config

    // Initialize services
    val userService = UserService()
/*    val listingService = ListingService()
    val bookingService = BookingService()
    val reviewService = ReviewService()
    val paymentService = PaymentService().apply { init(config) }
    val availabilityService = BookingAvailabilityService()*/


    routing {
        get("/") {
            call.respond(mapOf("status" to "ok", "service" to "Checkly Inn API"))
        }
        // Route modules
        authRoutes(userService)
        userRoutes(userService)
/*        listingRoutes(listingService)
        bookingRoutes(bookingService, availabilityService)
        reviewRoutes(reviewService)
        paymentRoutes(paymentService, bookingService)
        webhookRoutes(paymentService, bookingService)
        imageRoutes(imageService, listingService)
        seedRoutes()*/
    }

}