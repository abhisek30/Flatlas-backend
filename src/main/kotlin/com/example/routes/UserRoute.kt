package com.example.routes


import com.example.models.User
import com.example.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/users") {
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val user = userService.read(id)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respondText("User not found", status = HttpStatusCode.NotFound)
                }
            } else {
                call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
            }
        }
        post {
            val user = call.receive<User>()
            val id = userService.create(user)
            call.respondText("User created with ID: $id", status = HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val user = call.receive<User>()
                userService.update(id, user)
                call.respondText("User updated", status = HttpStatusCode.OK)
            } else {
                call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
            }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                userService.delete(id)
                call.respondText("User deleted", status = HttpStatusCode.OK)
            } else {
                call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
            }
        }
    }

}