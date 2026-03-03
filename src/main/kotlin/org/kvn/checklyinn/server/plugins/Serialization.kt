package org.kvn.checklyinn.server.plugins

fun Application.configureSerialization() {
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}