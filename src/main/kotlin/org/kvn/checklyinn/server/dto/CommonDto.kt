package org.kvn.checklyinn.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class ListingSearchResponse(
    val listings: List<ListingResponse>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class AvailabilityCheckResponse(
    val available: Boolean,
    val reason: String? = null,
    val priceCalculation: PriceCalculationResponse? = null
)

@Serializable
data class PriceCalculationResponse(
    val subtotal: Double,
    val taxes: Double,
    val serviceFee: Double,
    val total: Double,
    val currency: String,
    val numberOfNights: Int,
    val numberOfGuests: Int
)

@Serializable
data class ImageInfo(
    val url: String,
    val fileName: String,
    val size: Long,
    val contentType: String? = null
)

@Serializable
data class ImageInfoResponse(
    val message: String,
    val image: ImageInfo
)

@Serializable
data class SingleImageUploadResponse(
    val message: String,
    val sampleListings: List<String>
)