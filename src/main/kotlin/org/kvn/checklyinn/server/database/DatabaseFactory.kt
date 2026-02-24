package org.kvn.checklyinn.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("storage.driverClassName").getString()
        val jdbcURL = config.property("storage.jdbcURL").getString()
        val username = config.propertyOrNull()("storage.username")?.getString()?: ""
        val password = config.propertyOrNull("storage.password")?.getString() ?: ""
        val maxPoolSize = config.propertyOrNull("storage.maximumPoolSize")?.getString()?.toInt() ?: 10


    }

    private fun createHikariDataSource(
        url: String,
        driver: String,
        username: String = "",
        password: String = "",
        maxPoolSize: Int = 10
    ): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            maximumPoolSize = maxPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            // Set credentials if provided (required for MySQL/PostgreSQL)
            if (username.isNotEmpty()) {
                this.username = username
            }
            if (password.isNotEmpty()) {
                this.password = password
            }

            // MySQL-specific optimizations
            if(driver.contains("mysql", ignoreCase = true)) {
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                addDataSourceProperty("useServerPrepStmts", "true")
                addDataSourceProperty("useLocalSessionState", "true")
                addDataSourceProperty("rewriteBatchedStatements", "true")
                addDataSourceProperty("cacheResultSetMetadata", "true")
                addDataSourceProperty("cacheServerConfiguration", "true")
                addDataSourceProperty("elideSetAutoCommits", "true")
                addDataSourceProperty("maintainTimeStats", "false")
            }

            validate()
        }

        return HikariDataSource(hikariConfig)

    }

    suspend fun <T> dbQuery(block: suspend() -> T):T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

}