package io.github.mayachen350.goodolServer.data.services

import arrow.core.nel
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
import org.jetbrains.exposed.v1.r2dbc.*
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
            TasksTable.selectAll().where { TasksTable.isDeleted eq false }.toList()
        }

        suspend fun existsByName(name: String): Boolean = suspendTransaction(database) {
            TasksTable.selectAll()
                .where { TasksTable.name eq name }
                .andWhere { TasksTable.isDeleted eq false }
                .toList().any()
        }

        /**Create a new task in the database IF it's not already present but deleted.
         *
         * If it is, toggle the deleted state. */
        suspend fun createNewTask(task: TaskDTO) {
            val taskIdThatExistedBefore: Int? = suspendTransaction {
                TasksTable.select(TasksTable.id)
                    .where { TasksTable.isDeleted eq true }
                    .andWhere { TasksTable.name eq task.name }
                    .singleOrNull()?.get(TasksTable.id)?.value
            }

            if (taskIdThatExistedBefore == null) {
                suspendTransaction(database) {
                    TasksTable.insert {
                        it[name] = task.name
                        it[categoryId] = task.categoryId
                    }
                }
            } else {
                suspendTransaction(database) {
                    TasksTable.update(where = {
                        TasksTable.id eq taskIdThatExistedBefore
                    }) {
                        it[isDeleted] = false
                    }
                }
            }
        }

        /**This function does *not* actually delete any task in the database, but *mark it* as deleted.
         *
         * This is to prevent accidental damage to the DB's content, and to be able to safely undo.
         *
         * Garbage collection for marked as deleted tasks could eventually be done later to not fill the database.
         * (Especially for tasks with no Todos assigned to it)*/
        suspend fun deleteTaskByName(name: String) = suspendTransaction(database) {
            TasksTable.update(where = {
                TasksTable.name eq name
            }) {
                it[isDeleted] = true
            }
        }
    }
}
