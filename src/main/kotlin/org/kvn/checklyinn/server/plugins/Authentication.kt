package org.kvn.checklyinn.server.plugins

import io.ktor.server.application.*

fun Application.configureAuthentication(config: ApplicationConfig) {
    JwtConfig.init(config)

    install(Authentication)
        jwt("auth-jwt") {
            realm = JwtConfig.getRealm()
            verifier(
            )
        }

}