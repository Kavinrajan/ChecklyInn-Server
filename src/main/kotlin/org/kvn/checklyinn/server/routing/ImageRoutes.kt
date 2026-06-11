package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.services.ImageService
import org.kvn.checklyinn.server.services.ListingService

fun Route.imageRoutes(imageService: ImageService, listingService: ListingService) {
    // Public route to serve uploaded images
    route("/uploads") {
        get("/{subdirectory}/{filename}") {
            val subdirectory = call.parameters["subdirectory"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing subdirectory")
                return@get
            }
        }
        // Implement image upload, retrieval, and deletion endpoints here
    }

    // Authenticated image management routes
    route("/images") {
        authenticate("auth-jwt") {
            // Upload image for listing
            post("/listings/{listingId}") {
                val principal = call.principal<JWTPrincipal>()

            }

            // Upload user profile image
            post("/users/profile") {

            }

            // Delete image
            delete("/listings") {

            }

            // Get listing images
            get("/listings/{listingId}") {

            }

        }
    }
}