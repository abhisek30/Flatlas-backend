package com.example

import com.example.utils.initializeFirebase
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    initializeFirebase()
    configureAuthentication()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
