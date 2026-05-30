package io.github.mayachen350.goodolServer.data.services

import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object WeeklyTaskingService {
    suspend fun getAllPeople() = suspendTransaction {
        ResponsiblesTable.selectAll().toList()
    }
}