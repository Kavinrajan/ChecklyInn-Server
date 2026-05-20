package org.kvn.checklyinn.server.dto

@Serializable
data class CreateReviewRequest(
    val listingId: String,
    val bookingId: String? = null,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null
)

@Serializable
data class ReviewResponse(
    val id: String,
    val customerId: String,
    val listingId: String,
    val bookingId: String? = null,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val isVerified: Boolean,
    val isApproved: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val customerName: String? = null
)
