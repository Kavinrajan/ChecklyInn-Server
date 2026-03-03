package org.kvn.checklyinn.server.services

class UserService {
    suspend fun createUser(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?,
        phone: String?,
        role: String
    ): UserResponse {
        // Implementation to create a new user in the database
    }
}