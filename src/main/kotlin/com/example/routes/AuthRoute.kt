package com.example.routes

import com.example.models.User
import com.example.services.UserService
import com.example.utils.generateToken
import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userService: UserService) {
    post("/login") {
        val idToken = call.request.headers["idToken"]
        if (idToken != null) {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            val user = userService.findByEmail(decodedToken.email)
            if (user != null) {
                val token = generateToken(user.id!!)
                call.respond(mapOf("message" to "User logged in with ID: ${user.id}", "token" to token))
            } else {
                val newUser = User(name = decodedToken.name, email = decodedToken.email, uid = decodedToken.uid)
                val userId = userService.create(newUser)
                val token = generateToken(userId)
                call.respond(mapOf("message" to "User registered with ID: $userId", "token" to token))
            }
        } else {
            call.respondText("Invalid token", status = HttpStatusCode.BadRequest)
        }
    }
    post("/register") {
        val idToken = call.request.headers["idToken"]
        if (idToken != null) {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            val existingUser = userService.findByEmail(decodedToken.email)
            if (existingUser != null) {
                call.respondText("User with this UID already exists", status = HttpStatusCode.Conflict)
            } else {
                val user = User(name = decodedToken.name, email = decodedToken.email, uid = decodedToken.uid)
                val userId = userService.create(user)
                val token = generateToken(userId)
                call.respond(mapOf("message" to "User registered with ID: $userId", "token" to token))
            }
        } else {
            call.respondText("Invalid token", status = HttpStatusCode.BadRequest)
        }
    }
}