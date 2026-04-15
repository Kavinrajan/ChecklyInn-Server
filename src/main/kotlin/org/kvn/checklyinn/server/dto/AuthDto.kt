package org.kvn.checklyinn.server.dto

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val role: String = "CUSTOMER"
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

@Ser            ializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val role: String,
    val isActive: Boolean
)