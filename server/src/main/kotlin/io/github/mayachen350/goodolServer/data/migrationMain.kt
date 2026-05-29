package io.github.mayachen350.goodolServer.data

import io.github.mayachen350.goodolServer.data.tables.WeeklyTaskingTables
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.migration.r2dbc.MigrationUtils
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

@OptIn(ExperimentalDatabaseMigrationApi::class)
suspend fun main() {
    // TODO: Change to configureExposed later on

    val database = R2dbcDatabase.connect(
        url = "r2dbc:mariadb://localhost:3306/GoodolDb",
        user = "dev",
        password = "password",
    )

    suspendTransaction {
        MigrationUtils.generateMigrationScript(
            tables = WeeklyTaskingTables,
            "migrations",
            scriptName = "V1__AddWeeklyTaskingTables"
        )
    }
}