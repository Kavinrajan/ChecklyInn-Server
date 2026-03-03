package org.kvn.checklyinn.server.plugins

fun Application.configureSwagger() {
    install(io.ktor.server.plugins.swagger.Swagger) {
        swagger {
            info {
                title = "ChecklyInn API"
                version = "1.0.0"
                description = "API documentation for ChecklyInn server"
            }
        }
    }
}