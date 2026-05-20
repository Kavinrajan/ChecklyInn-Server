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

    )

}