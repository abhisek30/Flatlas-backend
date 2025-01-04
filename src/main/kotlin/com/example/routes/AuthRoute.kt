package com.example.routes

import com.example.models.User
import com.example.services.UserService
import com.example.utils.generateToken
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userService: UserService) {
    post("/login") {
        val user = call.receive<User>()
        val authenticatedUser = userService.authenticate(user.email, user.passwordHash.orEmpty())
        if (authenticatedUser != null) {
            val token = generateToken(authenticatedUser.id!!)
            call.respond(mapOf("token" to token))
        } else {
            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }
    post("/register") {
        val user = call.receive<User>()
        val existingUser = userService.findByEmail(user.email)
        if (existingUser != null) {
            call.respondText("User with this email already exists", status = HttpStatusCode.Conflict)
        } else {
            val userId = userService.create(user)
            val token = generateToken(userId)
            call.respond(mapOf("message" to "User registered with ID: $userId", "token" to token))
        }
    }
}