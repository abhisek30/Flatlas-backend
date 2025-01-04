package com.example.routes


import com.example.models.Flat
import com.example.services.FlatService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.flatRoutes(flatService: FlatService) {
    authenticate("auth-jwt") {
        route("/flats") {
            get {
                call.respond(flatService.getAllFlats())
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    val flat = flatService.read(id)
                    if (flat != null) {
                        call.respond(flat)
                    } else {
                        call.respondText("Flat not found", status = HttpStatusCode.NotFound)
                    }
                } else {
                    call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                }
            }
            post {
                val flat = call.receive<Flat>()
                val id = flatService.create(flat)
                call.respondText("Flat created with ID: $id", status = HttpStatusCode.Created)
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    val flat = call.receive<Flat>()
                    flatService.update(id, flat)
                    call.respondText("Flat updated", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                }
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    flatService.delete(id)
                    call.respondText("Flat deleted", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                }
            }
            get("/search") {
                val latitude = call.request.queryParameters["latitude"]?.toDoubleOrNull()
                val longitude = call.request.queryParameters["longitude"]?.toDoubleOrNull()
                val radius = call.request.queryParameters["radius"]?.toDoubleOrNull() ?: 10.0 // Default radius 10 km
                if (latitude != null && longitude != null) {
                    call.respond(flatService.searchFlatsNearby(latitude, longitude, radius))
                } else {
                    call.respondText("Invalid or missing latitude/longitude", status = HttpStatusCode.BadRequest)
                }
            }
        }
    }
}