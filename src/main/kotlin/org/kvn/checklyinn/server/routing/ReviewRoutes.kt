package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.services.ReviewService

fun Route.reviewRoutes(reviewService: ReviewService) {
    route("/reviews") {
        // Public endpoints
        get("/listings/{listingId}") {

        }

        // Authenticated endpoints
        authenticate("auth-jwt") {
            post {

            }

            get("/my-reviews") {

            }

            get("/{id}") {

            }

            put("/{id}") {

            }

            delete("/{id}") {

            }

            // Admin only
            put("/{id}/approve") {

            }

        }

    }

}