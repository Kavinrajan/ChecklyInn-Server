package org.kvn.checklyinn.server

import org.kvn.checklyinn.server.database.DatabaseFactory
import org.kvn.checklyinn.server.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = environment.config
    
    // Initialize database
    DatabaseFactory.init(config)
    // Initialize services
    val imageService = org.kvn.checklyinn.server.services.ImageService().apply { init(config) }
    
    // Configure plugins
    configureSerialization()
    configureAuthentication(config)
    configureCORS()
    configureStatusPages()
    configureSwagger()
    
    // Configure routing
    configureRouting(imageService)
}

