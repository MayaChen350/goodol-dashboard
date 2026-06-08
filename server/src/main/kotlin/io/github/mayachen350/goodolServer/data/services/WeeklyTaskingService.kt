package io.github.mayachen350.goodolServer.data.services

import io.github.mayachen350.goodolServer.application.database
import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.WeekDTO
import io.github.mayachen350.goodolServer.utils.atEndOfWeek
import io.github.mayachen350.goodolServer.utils.atStartOfWeek
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object WeeklyTaskingService {
    object People {
        suspend fun getAllPeople() = suspendTransaction(database) {
            ResponsiblesTable.selectAll().toList()
        }
    }

    object Weeks {
        suspend fun createNewWeek(weekDTO: WeekDTO) = suspendTransaction(database) {
            WeeksTable.insert {
                it[id] = weekDTO.weekId
                it[startDate] = weekDTO.date
                it[endDate] = weekDTO.date.atEndOfWeek()
            }
        }

        /**
         * This function calculates which ID the Week should have before creating it and returning the ID.
         */
        suspend fun createNewWeekFromDate(date: LocalDate): UInt {
            val correctedDate = date.atStartOfWeek()

            val oldestRegisteredWeek: WeekDTO? = suspendTransaction(database) {
                WeeksTable
                    .selectAll()
                    .orderBy(WeeksTable.startDate)
                    .map { WeekDTO(it[WeeksTable.id], it[WeeksTable.startDate]) }
                    .firstOrNull()
            }

            val weekId = if (oldestRegisteredWeek != null) {
                oldestRegisteredWeek.weekId + (oldestRegisteredWeek.date.daysUntil(correctedDate).floorDiv(7)
                    .toUInt())
            } else 1u

            createNewWeek(WeekDTO(weekId, correctedDate))
            return weekId
        }

        suspend fun getByDate(date: LocalDate) = suspendTransaction(database) {
            val correctedDate = date.atStartOfWeek()

            WeeksTable.select(WeeksTable.id).where { WeeksTable.startDate eq correctedDate }.singleOrNull()
        }
    }

    object Tasks {
        suspend fun getAllTasks() = suspendTransaction(database) {
            TasksTable.selectAll().toList()
        }

        suspend fun createNewTask(task: TaskDTO) = suspendTransaction(database) {
            TasksTable.insert {
                it[name] = task.name
                it[categoryId] = task.categoryId
            }
        }
    }
}
