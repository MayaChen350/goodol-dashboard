package io.github.mayachen350.goodolServer.data.services

import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.WeekDTO
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object WeeklyTaskingService {
    suspend fun getAllPeople() = suspendTransaction {
        ResponsiblesTable.selectAll().toList()
    }

    suspend fun createNewWeek(weekDTO: WeekDTO) = suspendTransaction {
        WeeksTable.insert {
            it[id] = weekDTO.weekId
            it[startDate] = weekDTO.date
            it[endDate] = weekDTO.date.plus(1, DateTimeUnit.WEEK)
        }
    }
}