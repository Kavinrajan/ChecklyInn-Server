package org.kvn.checklyinn.server.routing

import org.kvn.checklyinn.server.dto.LoginRequest
import org.kvn.checklyinn.server.dto.RegisterRequest
import org.kvn.checklyinn.server.services.UserService

fun Route.authRoutes(userService: UserService) {
    route("/auth") {
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()

                    val user = userService.getUserByEmail(request.email)
                    if (user == null || !user.isActive) {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email"))
                        return@post
                    }

                    val isValid = userService.verifyPassword(request.email, request.password)
                    if (!isValid) {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password"))
                        return@post
                    }

                    val token = JwtConfig.generateToken(user)
                    call.respond(AuthResponse(token = token, user = user))

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Login failed"))
                }
            }

            post("/register") {
                try {
                    val request = call.receive<RegisterRequest>()

                    // Validate email format
                    if (!request.email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid email format"))
                        return@post
                    }

                    // Check if user exists
                    val existingUser = userService.getUserByEmail(request.email)
                    if (existingUser != null) {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("User with this email already exists"))
                        return@post
                    }

                    // Validate password
                    if (request.password.length < 6) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password must be at least 6 characters"))
                        return@post
                    }

                    val user = userService.createUser(
                        email = request.email,
                        password = request.password,
                        firstName = request.firstName,
                        lastName = request.lastName,
                        phone = request.phone,
                        role = request.role
                    )

                    val token = JwtConfig.generateToken(user)
                    call.respond(HttpStatusCode.Created, AuthResponse(token = token, user = user))

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Registration failed"))
                }
            }
    }
}