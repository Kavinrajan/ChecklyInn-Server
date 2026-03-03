package org.kvn.checklyinn.server.plugins

import io.ktor.server.application.*
import com.auth0.jwt.JWT

fun Application.configureAuthentication(config: ApplicationConfig) {
    JwtConfig.init(config)

    install(Authentication)
        jwt("auth-jwt") {
            realm = JwtConfig.getRealm()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JwtConfig.getSecret()))
                    .withAudience(JwtConfig.getAudience())
                    .withIssuer(JwtConfig.getIssuer())
                    .build()

            )
            validate { cred ->
                if (cred.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

}