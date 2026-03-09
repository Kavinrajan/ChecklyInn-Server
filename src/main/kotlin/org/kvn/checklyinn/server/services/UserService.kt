package org.kvn.checklyinn.server.services

import org.jetbrains.exposed.sql.insert
import org.kvn.checklyinn.server.database.DatabaseFactory
import org.kvn.checklyinn.server.dto.UserResponse
import org.kvn.checklyinn.server.models.UserRole
import org.kvn.checklyinn.server.models.Users
import org.mindrot.jbcrypt.BCrypt
import java.util.*


class UserService {
    suspend fun createUser(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?,
        phone: String?,
        role: String
    ): UserResponse = DatabaseFactory.dbQuery {
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val userRole = try {
            UserRole.valueOf(role.uppercase())
        } catch (e: IllegalArgumentException) {
            UserRole.CUSTOMER
        }

        val id = Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.firstName] = firstName
            it[Users.lastName] = lastName
            it[Users.phone] = phone
            it[Users.role] = userRole.name
        }[Users.id].value

        val userRow = Users.select { Users.id eq id }
            .singleOrNull()
            ?: throw IllegalStateException("Failed to retrieve created user")

        rowToUser(userRow)
    }

    suspend fun getUserByEmail(email: String): UserResponse? = DatabaseFactory.dbQuery {
        Users.select { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun getUserById(id: String): UserResponse? = DatabaseFactory.dbQuery {
        Users.selectAll().where { Users.id eq UUID.fromString(id) }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    private fun rowToUser(row: ResultRow): UserResponse {
        return UserResponse(
            id = row[Users.id].value.toString(),
            email = row[Users.email],
            firstName = row[Users.firstName],
            lastName = row[Users.lastName],
            phone = row[Users.phone],
            role = row[Users.role],
            isActive = row[Users.isActive]
        )
    }

}