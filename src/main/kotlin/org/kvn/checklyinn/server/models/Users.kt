package org.kvn.checklyinn.server.models

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100).nullable()
    val lastName = varchar("last_name", 100).nullable()
    val phone = varchar("phone", 20).nullable()
    val role = varchar("role", 20).default("CUSTOMER") // CUSTOMER, VENDOR, ADMIN
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
}

enum class UserRole {
    CUSTOMER,
    VENDOR,
    ADMIN
}