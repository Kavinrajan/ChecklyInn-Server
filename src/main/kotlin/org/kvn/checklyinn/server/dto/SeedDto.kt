package org.kvn.checklyinn.server.dto

import javax.validation.constraints.Email

@Serlaizable
data class TestUserInfo(
    val email: String,
    val password: String,
    val role: String? = null
)

@Serlaizable
data class SeedResponse(
    val message: String,
    val testUserInfo: Map<String, TestUserInfo>,
    val note: String? = null
)

@Serlaizable
data class SeedInfoResponse(
    val message: String,
    val testUserInfo: Map<String, TestUserInfo>,
    val sampleListings: List<String>
)