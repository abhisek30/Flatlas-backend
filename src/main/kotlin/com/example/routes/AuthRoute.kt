package com.example.routes

import com.example.models.User
import com.example.services.UserService
import com.example.utils.generateToken
import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userService: UserService) {
    post("/login") {
        val idToken = call.request.headers["idToken"]
        val request = call.receive<User>()
        if (idToken != null) {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            val user = userService.findByEmail(decodedToken.email)
            if (user != null) {
                val token = generateToken(user.id!!)
                call.respond(mapOf("userId" to "${user.id}", "token" to token))
            } else {
                val newUser = User(name = decodedToken.name, email = decodedToken.email, uid = decodedToken.uid, fcmToken = request.fcmToken)
                val userId = userService.create(newUser)
                val token = generateToken(userId)
                call.respond(mapOf("userId" to "$userId", "token" to token))
            }
        } else {
            call.respondText("Invalid token", status = HttpStatusCode.BadRequest)
        }
    }
    post("/register") {
        val idToken = call.request.headers["idToken"]
        val request = call.receive<User>()
        if (idToken != null) {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            val existingUser = userService.findByEmail(decodedToken.email)
            if (existingUser != null) {
                val token = generateToken(existingUser.id!!)
                call.respond(mapOf("userId" to existingUser.id, "token" to token))
            } else {
                val user = User(name = decodedToken.name, email = decodedToken.email, uid = decodedToken.uid, fcmToken = request.fcmToken)
                val userId = userService.create(user)
                val token = generateToken(userId)
                call.respond(mapOf("userId" to userId, "token" to token))
            }
        } else {
            call.respondText("Invalid token", status = HttpStatusCode.BadRequest)
        }
    }
}