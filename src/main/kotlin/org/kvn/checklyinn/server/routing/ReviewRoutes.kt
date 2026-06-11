package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.dto.CreateReviewRequest
import org.kvn.checklyinn.server.services.ReviewService

fun Route.reviewRoutes(reviewService: ReviewService) {
    route("/reviews") {
        // Public endpoints
        get("/listings/{listingId}") {
            val listingId = call.parameters["listingId"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            val reviews = reviewService.getReviewsByListing(listingId, page, pageSize)
            call.respond(reviews)
        }

        // Authenticated endpoints
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                try {
                    val request = call.receive<CreateReviewRequest>()
                    val review = reviewService.createReview(
                        customerId = userId,
                        listingId = request.listingId,
                        bookingId = request.bookingId,
                        rating = request.rating,
                        title = request.title,
                        comment = request.comment
                    )

                    call.respond(HttpStatusCode.Created, review)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Bad request"))
                }

            }

            get("/my-reviews") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                val reviews = reviewService.getReviewsByCustomer(userId, page, pageSize)
                call.respond(reviews)
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val review = reviewService.getReviewById(id)
                if (review == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respond(review)
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }

                val review = reviewService.getReviewById(id)
                if (review == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                val review = reviewService.getReviewById(id)
                if (review == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                // Check if user owns the review or is admin
                if (review.customerId != userId && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@delete
                }

                val deleted = reviewService.deleteReview(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // Admin only
            put("/{id}/approve") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                if (role != "admin") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put
                }

                val approvedReview = reviewService.approveReview(id)
                if (approvedReview == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                call.respond(approvedReview)
            }

        }

    }

}