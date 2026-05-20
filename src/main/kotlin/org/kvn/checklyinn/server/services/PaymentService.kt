package org.kvn.checklyinn.server.services

import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.kvn.checklyinn.server.database.DatabaseFactory
import org.kvn.checklyinn.server.models.Bookings
import org.kvn.checklyinn.server.models.PaymentStatus
import java.util.UUID

class PaymentService {

    private var stripeSecretKey: String? = null
    private var webhookSecret: String? = null

    fun init(config: ApplicationConfig) {
        stripeSecretKey = config.property("stripe.secretKey").getString()
        webhookSecret = config.property("stripe.webhookSecret").getString()
        String.apiKey = stripeSecretKey
    }

    suspend fun createPaymentIntent(
        bookingId: String,
        amount: Long, // Amount in cents
        currency: String = "usd",
        customerId: String? = null
    ): PaymentIntentResponse = DatabaseFactory.dbQuery {
        val booking = Bookings.select { Bookings.id eq UUID.fromString(bookingId) }.singleOrNull()
            ?: throw IllegalArgumentException("Booking not found")

        try {
            val paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency.lowercase())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )

            // Add metadata
            paramsBuilder.putMetadata("bookingId", bookingId)
            paramsBuilder.putMetadata("customerId", booking[Bookings.customerId].toString())

            customerId?.let { paramsBuilder.setCustomer(it) }
            val params = paramsBuilder.build()
            val paymentIntent = PaymentIntent.create(params)

            Bookings.update({ Bookings.id eq UUID.fromString(bookingId) }) {
                it[Bookings.paymentId] = paymentIntent.id
            }

            PaymentIntentResponse(
                clientSecret = paymentIntent.clientSecret,
                paymentIntentId = paymentIntent.id,
                amount = paymentIntent.amount,
                currency = paymentIntent.currency,
                status = paymentIntent.status
            )
        } catch(e: StripeException) {
            throw PaymentException("Failed to create payment intent: ${e.message}", e)
        }

    }

    suspend fun confirmPayment(paymentIntentId: String): PaymentIntent? {
        return try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)

            if(paymentIntent.status == "succeeded") {
                // Update booking payment status
                val bookingId = paymentIntent.metadata["bookingId"]
                if (bookingId != null) {
                    DatabaseFactory.dbQuery {
                        Bookings.update({ Bookings.id eq UUID.fromString(bookingId) }) {
                            it[Bookings.paymentStatus] = PaymentStatus.PAID.name
                        }
                    }
                }
            }
            paymentIntent
        } catch (e: StripeException) {
            throw PaymentException("Failed to confirm payment intent: ${e.message}", e)
        }
    }

    suspend fun processRefund(
        bookingId: String,
        amount: Long? = null, // If null, full refund
        reason: String? = null
    ): RefundResponse = DatabaseFactory.dbQuery {
        val booking = Bookings.select { Bookings.id eq UUID.fromString(bookingId) }.singleOrNull()
            ?: throw IllegalArgumentException("Booking not found")

        val paymentId = booking[Bookings.paymentId]
            ?: throw IllegalArgumentException("No payment associated with this booking")

        if(booking[Bookings.paymentStatus] != PaymentStatus.PAID.name) {
            throw IllegalStateException("Cannot refund a booking that is not paid")
        }

        try {
            val params = RefundCreateParams.builder()
                .setPaymentIntent(paymentId)
                .apply {
                    amount?.let { setAmount(it) }
                    reason?.let { setReason(RefundCreateParams.Reason.valueOf(it.uppercase())) }
                }
                .build()

            val refund = Refund.create(paramsBuilder.build())

            // Update booking payment status
            Bookings.update({ Bookings.id eq UUID.fromString(bookingId) }) {
                it[Bookings.paymentStatus] = PaymentStatus.REFUNDED.name
            }

            RefundResponse(
                refundId = refund.id,
                amount = refund.amount,
                currency = refund.currency,
                status = refund.status,
                reason = refund.reason
            )
        } catch (e: StripeException) {
            throw PaymentException("Failed to process refund: ${e.message}", e)
        }

    }
    suspend fun getPaymentIntent(paymentIntentId: String): PaymentIntent? {
        return try {
            PaymentIntent.retrieve(paymentIntentId)
        } catch (e: StripeException) {
            throw PaymentException("Failed to retrieve payment intent: ${e.message}", e)
        }
    }

    fun getWebhookSecret(): String? = webhookSecret

}

data class PaymentIntentResponse(
    val clientSecret: String,
    val paymentIntentId: String,
    val amount: Long,
    val currency: String,
    val status: String
)

data class RefundResponse(
    val refundId: String,
    val amount: Long,
    val currency: String,
    val status: String,
    val reason: String?
)
class PaymentException(message: String, cause: Throwable? = null) : Exception(message, cause)