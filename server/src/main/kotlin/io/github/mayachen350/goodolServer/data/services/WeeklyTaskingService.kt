package io.github.mayachen350.goodolServer.data.services

import io.github.mayachen350.goodolServer.data.services.ExposedUserService.Users
import io.github.mayachen350.goodolServer.data.tables.WeeklyTaskingDbSchema
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class WeeklyTaskingService private constructor(val database: R2dbcDatabase) {
    companion object {
        suspend fun initiated(database: R2dbcDatabase): WeeklyTaskingService = WeeklyTaskingService(database).also {
            // Create schema and tables
            suspendTransaction(database) {
                SchemaUtils.createSchema(WeeklyTaskingDbSchema)
                SchemaUtils.setSchema(WeeklyTaskingDbSchema)

                SchemaUtils.create(Users)
            }
        }
    }
}