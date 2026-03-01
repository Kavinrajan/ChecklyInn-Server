package org.kvn.checklyinn.server.services

import java.io.File
import java.util.*

class ImageService {
    private var uploadDirectory: String = "./uploads"
    private var baseUrl: String = "http://localhost:8080"
    private var maxFileSizeBytes: Long = 10 * 1024 * 1024 // 10MB default
    private val allowedMimeTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif")

    fun init(config: ApplicationConfig) {
        uploadDirectory = config.property("uploads.directory").getString()
        baseUrl = config.property("uploads.baseUrl").getString()
        val maxFileSizeMB = config.property("uploads.maxFileSize").getString().toIntOrNull() ?: 10
        maxFileSizeBytes = maxFileSizeMB * 1024L * 1024L

        // Create upload directory if it doesn't exist
        val uploadDir = File(uploadDirectory)
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }

        File(uploadDir, "listings").mkdirs()
        File(uploadDir, "users").mkdirs()

    }

    suspend fun uploadImage(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        listingId: String? = null,
        userId: String? = null
    ) {
        // Validate file type
        if (!allowedMimeTypes.contains(contentType.lowercase())) {
            throw ImageUploadException("Invalid file type. Allowed types: ${allowedMimeTypes.joinToString()}")
        }

        // Validate file size
        if (fileBytes.size > maxFileSizeBytes) {
            throw ImageUploadException("File size exceeds maximum allowed size of ${maxFileSizeBytes / (1024 * 1024)}MB")
        }

        // Generate unique filename
        val fileExtension = fileName.substringAfterLast('.', "")
        val uniqueFileName = "${UUID.randomUUID()}.$fileExtension"

        // Determine subdirectory
        val subdirectory = when {
            listingId != null -> "listings"
            userId != null -> "users"
            else -> { "general" }
        }

        val targetDir = File(uploadDirectory, subdirectory)
        targetDir.mkdirs()

        val targetFile = File(targetDir, uniqueFileName)
        targetFile.writeBytes(fileBytes)

        // Generate URL
        val imageUrl = "$baseUrl/uploads/$subdirectory/$uniqueFileName"

        // If associated with listing, add to listing's images
        if (listingId != null) {
            addImageToListing(listingId, imageUrl)
        }

        return ImageUploadResult(
            url = imageUrl,
        )

    }

}

class ImageUploadException(message: String) : Exception(message)