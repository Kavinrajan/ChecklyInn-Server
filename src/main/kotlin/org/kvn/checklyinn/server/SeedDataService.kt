package org.kvn.checklyinn.server

import org.kvn.checklyinn.server.database.DatabaseFactory

class SeedDataService {

    suspend fun seedDatabase() {
        DatabaseFactory.dbQuery {
            // Seed Users

        }
    }


}