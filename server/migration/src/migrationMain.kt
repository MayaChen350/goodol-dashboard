package io.github.mayachen350.goodolServer

import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.migration.r2dbc.MigrationUtils
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

@OptIn(ExperimentalDatabaseMigrationApi::class)
suspend fun main(args: Array<String>) {
    // TODO: Change to configureExposed later on
    // TODO: what the fuck does the earlier todo meant

    val migrationName = run {
        val migrationVersion = args[0]
        val migrationName = args[1]

        "V${migrationVersion.replace('.', '_')}__${migrationName}"
    }

    println("Migration with name: $migrationName able to be created.")

    val database = R2dbcDatabase.connect(
        url = _root_ide_package_.io.github.mayachen350.goodolServer.application.MariaDBConnection.URL,
        user = _root_ide_package_.io.github.mayachen350.goodolServer.application.MariaDBConnection.USER,
        password = _root_ide_package_.io.github.mayachen350.goodolServer.application.MariaDBConnection.PASSWORD,
    )

    suspendTransaction {
        MigrationUtils.generateMigrationScript(
            tables = _root_ide_package_.io.github.mayachen350.goodolServer.data.tables.WeeklyTaskingTables,
            "src/main/resources/db/migration",
            scriptName = migrationName
        )
    }
}