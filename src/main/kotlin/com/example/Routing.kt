package com.example

import com.example.routes.flatRoutes
import com.example.services.FlatService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting() {
    val database = Database.connect(
        url = environment.config.property("postgres.url").getString(),
        user = environment.config.property("postgres.user").getString(),
        password = environment.config.property("postgres.password").getString(),
    )
    log.info("Connecting to postgres database at ${environment.config.property("postgres.url").getString()}")
    val flatService = FlatService(database)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        flatRoutes(flatService)
    }
}
