package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.dto.CreateListingRequest
import org.kvn.checklyinn.server.services.ListingService

fun Route.ListingRoutes(listingService: ListingService) {
    route("/listings") {
        // Public endpoints
        get {
            val category = call.request.queryParameters["category"]
            val location = call.request.queryParameters["location"]
            val city = call.request.queryParameters["city"]
            val country = call.request.queryParameters["country"]
            val minPrice = call.request.queryParameters["minPrice"]?.toDoubleOrNull()
            val maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull()
            val minRating = call.request.queryParameters["minRating"]?.toDoubleOrNull()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            try {
                val (listings, totalCount) = listingService.searchListings(
                    category = category,
                    location = location,
                    city = city,
                    country = country,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    minRating = minRating,
                    page = page,
                    pageSize = pageSize
                )

                call.respond(
                    ListingSearchResponse(
                        listings = listings,
                        total = totalCount,
                        page = page,
                        pageSize = pageSize
                    )
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Internal server error"))
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            try {
                val listing = listingService.getListingById(id)
                if (listing != null) {
                    call.respond(listing)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Listing not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Internal server error"))
            }
        }

        // Authenticated endpoints (require authentication middleware)
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (`userId` == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token: userId claim missing"))
                    return@post
                }

                if (role != "VENDOR" && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only hosts can create listings"))
                    return@post
                }

                try {
                    val listingRequest = call.receive<CreateListingRequest>()
                    val listing = listingService.createListing(
                        vendorId = userId,
                        title = request.title,
                        description = request.description,
                        category = request.category,
                        location = request.location,
                        city = request.city,
                        country = request.country,
                        price = request.price,
                        currency = request.currency,
                        capacity = request.capacity,
                        availableFrom = request.availableFrom,
                        availableTo = request.availableTo,
                        images = request.images,
                        amenities = request.amenities
                    )
                    call.respond(HttpStatusCode.Created, newListing)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(e.message ?: "Internal server error")
                    )
                }
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing listing ID"))
                    return@put
                }

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }

                val listing = listingService.getListingById(id)
                if (listing == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                // Check if user owns the listing or is admin
                if (listing.vendorId != userId && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put
                }

                try {
                    val request = call.receive<UpdateListingRequest>()
                    val updatedListing = listingService.updateListing(
                        id = id,
                        title = request.title,
                        description = request.description,
                        location = request.location,
                        city = request.city,
                        country = request.country,
                        price = request.price,
                        currency = request.currency,
                        capacity = request.capacity,
                        availableFrom = request.availableFrom,
                        availableTo = request.availableTo,
                        images = request.images,
                        amenities = request.amenities,
                        isActive = request.isActive
                    )

                    if (updatedListing == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@put
                    }

                    call.respond(updatedListing)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Bad request"))
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

                val listing = listingService.getListingById(id)
                if (listing == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                // Check if user owns the listing or is admin
                if (listing.vendorId != userId && role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@delete

                }

                val deleted = listingService.deleteListing(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to delete listing"))
                }
            }

            get("/vendor/my-listings") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                try {
                    val listings = listingService.getListingsByVendor(userId)
                    call.respond(listings)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(e.message ?: "Internal server error")
                    )
                }
            }

        }
    }

}